package org.example.myexpensetracker.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.myexpensetracker.common.Config;
import org.example.myexpensetracker.controller.dialogs.*;
import org.example.myexpensetracker.dao.EntityDAO;
import org.example.myexpensetracker.dao.SummaryDAO;
import org.example.myexpensetracker.dao.UserDAO;
import org.example.myexpensetracker.dao.sql.*;
import org.example.myexpensetracker.db.dto.AccountTotalDTO;
import org.example.myexpensetracker.db.dto.ExpenseIncomeDTO;
import org.example.myexpensetracker.db.dto.RecordDTO;
import org.example.myexpensetracker.exchangerateproviders.implementations.FixerProvider;
import org.example.myexpensetracker.export.ContextXmlExporter;
import org.example.myexpensetracker.mappers.RecordMapper;
import org.example.myexpensetracker.mappers.SummaryMapper;
import org.example.myexpensetracker.model.Record;
import org.example.myexpensetracker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final UserDAO userDAO = new UserSQLDAO();
    private final EntityDAO<Account> accountDAO = new AccountSQLDAO();
    private final EntityDAO<Currency> currencyDAO = new CurrencySQLDAO();
    private final EntityDAO<Category> categoryDAO = new CategorySQLDAO();
    private final EntityDAO<Tag> tagDAO = new TagSQLDAO();
    private final EntityDAO<RecordDTO> recordDAO = new RecordSQLDAO();
    private final SummaryDAO summaryDAO = new SummarySQLDAO();

    private Context ctx;

    @FXML
    private TreeTableView<Account> accountTreeTableView;

    @FXML
    private TableView<Currency> currencyTableView;

    @FXML
    private TreeTableView<Category> categoryTreeTableView;

    @FXML
    private TableView<Tag> tagTableView;

    @FXML
    private TableView<Record> recordTableView;

    @FXML
    private TreeTableView<AccountTotal> accountTotalTreeTableView;

    @FXML
    private BarChart<String, Number> expensesBarChart;

    @FXML
    private TableView<ExchangeRate> exchangeRateTable;

    @FXML
    private TableColumn<ExchangeRate, String> currencyColumn;
    @FXML
    private TableColumn<ExchangeRate, String> directRateColumn;
    @FXML
    private TableColumn<ExchangeRate, String> inverseRateColumn;

    private ExchangeRateService exchangeRateService = new ExchangeRateService();

    @FXML
    public void initialize() {
        logger.info("Init MainController");
        setupAll();
    }

    @FXML
    void onEditProfile(ActionEvent event) {
        Optional<User.Profile> maybeProfile = new ProfileDialog(ctx).show(ctx.getUser().getProfile());
        maybeProfile.ifPresent(profile -> {
            ctx.getUser().setProfile(profile);
            userDAO.update(ctx.getUser());
        });
    }

    @FXML
    void onExportContext(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Context to");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showSaveDialog(getStage());
        if (selectedFile != null) {
            ContextXmlExporter.export(ctx, selectedFile);
        }
    }

    public void setContext(Context ctx) {
        logger.info("Set Context with user: {}", ctx.getUser());
        this.ctx = ctx;
        loadAll();
        setupExchangeRateService();
        refreshExchangeRates();

        ctx.findCurrencyById(ctx.getUser().getProfile().getBaseCurrencyId()).ifPresent(currency -> {
            directRateColumn.setText(currency.getCode() + " ->");
            inverseRateColumn.setText("-> " + currency.getCode());
        });
    }

    private void setupAll() {
        setupAccountTreeView();
        setupCurrencyTableView();
        setupCategoryTreeView();
        setupTagTableView();
        setupRecordTableView();
        setupExchangeRateTable();
    }

    private void loadAll() {
        reloadAccount();
        reloadCurrency();
        reloadCategory();
        reloadTag();
        refreshRecordTotalMonthly();
    }

    private void setupExchangeRateService() {
        exchangeRateService.setCtx(ctx);
        exchangeRateService.setExchangeRateProvider(new FixerProvider(Config.getInstance().getProperty("fixerApiKey")));
        exchangeRateService.setOnSucceeded(event -> {
            List<ExchangeRate> exchangeRates = exchangeRateService.getValue();
            if (exchangeRates != null) {
                exchangeRateTable.getItems().clear();
                exchangeRateTable.getItems().addAll(exchangeRates);
            }
        });
    }

    private void setupExchangeRateTable() {
        ContextMenu menu = new ContextMenu();
        MenuItem refreshItem = new MenuItem("Refresh");
        refreshItem.setOnAction(event -> {
            refreshExchangeRates();
        });
        menu.getItems().add(refreshItem);
        exchangeRateTable.setContextMenu(menu);

        currencyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExchangeRate, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExchangeRate, String> param) {
                return new SimpleStringProperty(param.getValue().getToCurrency().getCode());
            }
        });
        directRateColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExchangeRate, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExchangeRate, String> param) {
                return new SimpleStringProperty(param.getValue().getRate().toString());
            }
        });
        inverseRateColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExchangeRate, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExchangeRate, String> param) {
                return new SimpleStringProperty(param.getValue().invert().getRate().toString());
            }
        });
    }

    private void setupAccountTreeView() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addItem = new MenuItem("Add");
        addItem.setOnAction(event -> {
            Optional<Account> maybeAccount = new AccountDialog(ctx).show();
            maybeAccount.ifPresent(account -> {
                accountDAO.add(account);
                reloadAccount();
            });
        });
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(event -> {
            TreeItem<Account> selectedItem = accountTreeTableView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            Optional<Account> maybeAccount = new AccountDialog(ctx).show(selectedItem.getValue());
            maybeAccount.ifPresent(account -> {
                accountDAO.update(account);
                reloadAccount();
            });
        });
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            TreeItem<Account> selectedItem = accountTreeTableView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            Account selectedAccount = selectedItem.getValue();
            accountDAO.delete(selectedAccount.getId(), selectedAccount.getOwnerId());
            reloadAccount();
        });
        contextMenu.getItems().addAll(addItem, editItem, deleteItem);
        accountTreeTableView.setContextMenu(contextMenu);
    }

    private void setupCurrencyTableView() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addItem = new MenuItem("Add");
        addItem.setOnAction(event -> {
            Optional<Currency> maybeCurrency = new CurrencyDialog(ctx).show();
            maybeCurrency.ifPresent(currency -> {
                currency.setOrder(ctx.getCurrencies().size());
                currencyDAO.add(currency);
                reloadCurrency();
            });
        });
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(event -> {
            Currency selectedCurrency = currencyTableView.getSelectionModel().getSelectedItem();
            if (selectedCurrency == null) {
                return;
            }
            Optional<Currency> maybeCurrency = new CurrencyDialog(ctx).show(selectedCurrency);
            maybeCurrency.ifPresent(currency -> {
                currencyDAO.update(currency);
                reloadCurrency();
            });
        });
        MenuItem moveUpItem = new MenuItem("Move up");
        moveUpItem.setOnAction(event -> {
            int selectedIdx = currencyTableView.getSelectionModel().getSelectedIndex();
            if (selectedIdx == 0) {
                return;
            }
            Currency thisCurrency = ctx.getCurrencies().get(selectedIdx);
            thisCurrency.setOrder(selectedIdx - 1);
            Currency topCurrency = ctx.getCurrencies().get(selectedIdx - 1);
            topCurrency.setOrder(selectedIdx);
            currencyDAO.update(thisCurrency);
            currencyDAO.update(topCurrency);
            reloadCurrency();
        });
        MenuItem moveDownItem = new MenuItem("Move down");
        moveDownItem.setOnAction(event -> {
            int selectedIdx = currencyTableView.getSelectionModel().getSelectedIndex();
            if (selectedIdx == ctx.getCurrencies().size() - 1) {
                return;
            }
            Currency thisCurrency = ctx.getCurrencies().get(selectedIdx);
            thisCurrency.setOrder(selectedIdx + 1);
            Currency bottomCurrency = ctx.getCurrencies().get(selectedIdx + 1);
            bottomCurrency.setOrder(selectedIdx);
            currencyDAO.update(thisCurrency);
            currencyDAO.update(bottomCurrency);
            reloadCurrency();
        });
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            Currency selectedCurrency = currencyTableView.getSelectionModel().getSelectedItem();
            if (selectedCurrency == null) {
                return;
            }
            currencyDAO.delete(selectedCurrency.getId(), selectedCurrency.getOwnerId());
            reloadCurrency();
        });
        contextMenu.getItems().addAll(addItem, editItem, moveUpItem, moveDownItem, deleteItem);
        currencyTableView.setContextMenu(contextMenu);
    }

    private void setupCategoryTreeView() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addItem = new MenuItem("Add");
        addItem.setOnAction(event -> {
            Optional<Category> maybeCategory = new CategoryDialog(ctx).show();
            maybeCategory.ifPresent(category -> {
                categoryDAO.add(category);
                reloadCategory();
            });
        });
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(event -> {
            TreeItem<Category> selectedItem = categoryTreeTableView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            Category selectedCategory = selectedItem.getValue();
            Optional<Category> maybeCategory = new CategoryDialog(ctx).show(selectedCategory);
            maybeCategory.ifPresent(category -> {
                categoryDAO.update(category);
                reloadCategory();
            });
        });
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            TreeItem<Category> selectedItem = categoryTreeTableView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            Category selectedCategory = selectedItem.getValue();
            categoryDAO.delete(selectedCategory.getId(), selectedCategory.getOwnerId());
            reloadCategory();
        });
        contextMenu.getItems().addAll(addItem, editItem, deleteItem);
        categoryTreeTableView.setContextMenu(contextMenu);
    }


    private void setupTagTableView() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addItem = new MenuItem("Add");
        addItem.setOnAction(event -> {
            Optional<Tag> maybeTag = new TagDialog(ctx).show();
            maybeTag.ifPresent(tag -> {
                tagDAO.add(tag);
                reloadTag();
            });
        });
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(event -> {
            Tag selectedTag = tagTableView.getSelectionModel().getSelectedItem();
            if (selectedTag == null) {
                return;
            }
            Optional<Tag> maybeTag = new TagDialog(ctx).show(selectedTag);
            maybeTag.ifPresent(tag -> {
                tagDAO.update(tag);
                reloadTag();
            });
        });
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            Tag selectedTag = tagTableView.getSelectionModel().getSelectedItem();
            if (selectedTag == null) {
                return;
            }
            tagDAO.delete(selectedTag.getId(), selectedTag.getOwnerId());
            reloadTag();
        });
        contextMenu.getItems().addAll(addItem, editItem, deleteItem);
        tagTableView.setContextMenu(contextMenu);
    }

    private void setupRecordTableView() {
        ContextMenu contextMenu = new ContextMenu();
        Menu addMenu = new Menu("Add");
        MenuItem addExpenseItem = new MenuItem("Expense");
        addExpenseItem.setOnAction(event -> {
            Optional<Expense> maybeExpense = new ExpenseIncomeDialog(ctx).showAddExpense();
            logger.debug("Returned from dialog: {}", maybeExpense);
            maybeExpense.ifPresent(this::addRecord);
        });
        MenuItem addIncomeItem = new MenuItem("Income");
        addIncomeItem.setOnAction(event -> {
            Optional<Income> maybeIncome = new ExpenseIncomeDialog(ctx).showAddIncome();
            logger.debug("Returned from dialog: {}", maybeIncome);
            maybeIncome.ifPresent(this::addRecord);
        });
        MenuItem addExchangeItem = new MenuItem("Exchange");
        addExchangeItem.setOnAction(event -> {
            Optional<Exchange> maybeExchange = new ExchangeDialog(ctx).showAdd();
            logger.debug("Returned from dialog: {}", maybeExchange);
            maybeExchange.ifPresent(this::addRecord);
        });
        MenuItem addTransferItem = new MenuItem("Transfer");
        addTransferItem.setOnAction(event -> {
            Optional<Transfer> maybeTransfer = new TransferDialog(ctx).showAdd();
            logger.debug("Returned from dialog: {}", maybeTransfer);
            maybeTransfer.ifPresent(this::addRecord);
        });
        addMenu.getItems().addAll(addExpenseItem, addIncomeItem, addExchangeItem, addTransferItem);
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(event -> {
            Record selectedRecord = recordTableView.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                return;
            }
            logger.debug("Updating: {}", selectedRecord);
            switch (selectedRecord) {
                case Expense e:
                    Optional<Expense> maybeExpense = new ExpenseIncomeDialog(ctx).showEdit(e);
                    maybeExpense.ifPresent(this::updateRecord);
                    break;
                case Income i:
                    Optional<Income> maybeIncome = new ExpenseIncomeDialog(ctx).showEdit(i);
                    maybeIncome.ifPresent(this::updateRecord);
                    break;
                case Exchange ex:
                    Optional<Exchange> maybeExchange = new ExchangeDialog(ctx).showEdit(ex);
                    maybeExchange.ifPresent(this::updateRecord);
                    break;
                case Transfer tr:
                    Optional<Transfer> maybeTransfer = new TransferDialog(ctx).showEdit(tr);
                    maybeTransfer.ifPresent(this::updateRecord);
                    break;
            }
        });
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            Record selectedRecord = recordTableView.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                return;
            }
            recordDAO.delete(selectedRecord.getId(), ctx.getUserId());
            refreshRecordView();
        });
        contextMenu.getItems().addAll(addMenu, editItem, deleteItem);
        recordTableView.setContextMenu(contextMenu);
    }

    private void reloadAccount() {
        ctx.setAccounts(accountDAO.findAll(ctx.getUserId()));
        refreshAccountTreeView();
    }

    private void reloadCurrency() {
        ctx.setCurrencies(currencyDAO.findAll(ctx.getUserId()));
        refreshCurrencyView();
    }

    private void reloadCategory() {
        ctx.setCategories(categoryDAO.findAll(ctx.getUserId()));
        refreshCategoryTreeView();
    }

    private void reloadTag() {
        ctx.setTags(tagDAO.findAll(ctx.getUserId()));
        refreshTagView();
    }

    private void refreshAccountTreeView() {
        TreeItem<Account> root = buildTreeRoot(ctx.getAccounts(), new Account());
        root.setExpanded(true);
        accountTreeTableView.setRoot(root);
        accountTreeTableView.setShowRoot(false);
    }

    private void refreshCurrencyView() {
        currencyTableView.getItems().clear();
        currencyTableView.getItems().addAll(ctx.getCurrencies());
    }

    private void refreshCategoryTreeView() {
        TreeItem<Category> root = buildTreeRoot(ctx.getCategories(), new Category());
        root.setExpanded(true);
        categoryTreeTableView.setRoot(root);
        categoryTreeTableView.setShowRoot(false);
    }

    private void refreshTagView() {
        tagTableView.getItems().clear();
        tagTableView.getItems().addAll(ctx.getTags());
    }

    private void refreshRecordView() {
        List<RecordDTO> recordDTOS = recordDAO.findAll(ctx.getUserId());
        List<Record> records = recordDTOS.stream().map(recordDTO -> RecordMapper.dto2entity(recordDTO, ctx)).toList();
        recordTableView.getItems().clear();
        recordTableView.getItems().addAll(records);
    }

    private void addRecord(Record record) {
        logger.debug("Adding record");
        RecordDTO recordDTO = RecordMapper.entity2dto(record);
        recordDAO.add(recordDTO);
        refreshRecordTotalMonthly();
    }

    private void updateRecord(Record record) {
        logger.debug("Updating record");
        RecordDTO recordDTO = RecordMapper.entity2dto(record);
        recordDAO.update(recordDTO);
        refreshRecordTotalMonthly();
    }

    private void refreshRecordTotalMonthly() {
        refreshRecordView();
        refreshAccountTotal();
        refreshMonthlyExpenses();
    }

    private void refreshAccountTotal() {
        logger.debug("Refreshing account total");
        List<AccountTotalDTO> dtos = summaryDAO.getAccountTotals(ctx.getUserId());
        List<AccountTotal> accountTotals = SummaryMapper.mapAccountTotal(dtos, ctx);
        TreeItem<AccountTotal> root = buildTreeRoot(accountTotals, new AccountTotal());
        root.setExpanded(true);

        accountTotalTreeTableView.getColumns().clear();
        TreeTableColumn<AccountTotal, String> nameColumn = new TreeTableColumn<>("Account");
        nameColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<AccountTotal, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<AccountTotal, String> param) {
                return new SimpleStringProperty(param.getValue().getValue().getAccount().getName());
            }
        });
        accountTotalTreeTableView.getColumns().add(nameColumn);

        for (Currency currency : ctx.getCurrencies()) {
            TreeTableColumn<AccountTotal, String> amountColumn = new TreeTableColumn<>(currency.getCode());
            amountColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<AccountTotal, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<AccountTotal, String> param) {
                    BigDecimal amount = param.getValue().getValue().getTotals().get(currency);
                    return new SimpleStringProperty(amount != null ? amount.toString() : "0");
                }
            });
            accountTotalTreeTableView.getColumns().add(amountColumn);
        }

        accountTotalTreeTableView.setRoot(root);
        accountTotalTreeTableView.setShowRoot(false);
    }

    private void refreshMonthlyExpenses() {
        logger.debug("Refreshing monthly expenses");
        List<ExpenseIncomeDTO> expenseIncomeDTOs = summaryDAO.getExpenseIncome(ctx.getUserId());
        Map<String, List<MonthlyAmount>> monthlyMap = SummaryMapper.mapMonthlyExpenseIncome(expenseIncomeDTOs, ctx);

        expensesBarChart.getData().clear();
        for (var entry : monthlyMap.entrySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(entry.getKey());
            for (var item : entry.getValue()) {
                series.getData().add(new XYChart.Data<>(item.getMonth(), item.getAmount()));
            }
            expensesBarChart.getData().add(series);
        }
    }

    private void refreshExchangeRates() {
        exchangeRateService.restart();
    }

    private <T extends TreeStructure> TreeItem<T> buildTreeRoot(List<T> nodes, T empty) {
        Map<UUID, TreeItem<T>> map = nodes.stream().collect(Collectors.toMap(T::getId, TreeItem::new));
        TreeItem<T> root = new TreeItem<>(empty);

        for (T node : nodes) {
            TreeItem<T> treeItem = map.get(node.getId());
            if (node.getParentId() == null) {
                root.getChildren().add(treeItem);
            } else {
                TreeItem<T> parentItem = map.get(node.getParentId());
                if (parentItem != null) {
                    parentItem.getChildren().add(treeItem);
                }
            }
        }

        return root;
    }

    private Stage getStage() {
        return (Stage) accountTreeTableView.getScene().getWindow();
    }
}