package com.jdc.pos.views;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.ToIntFunction;

import com.jdc.pos.entities.Category;
import com.jdc.pos.entities.Item;
import com.jdc.pos.entities.OrderDetails;
import com.jdc.pos.entities.Voucher;
import com.jdc.pos.service.ItemService;
import com.jdc.pos.service.SaleService;
import com.jdc.pos.util.MessageHandler;
import com.jdc.pos.util.PosException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class Pos implements Initializable {

    @FXML
    private ComboBox<Category> category;
    @FXML
    private TextField idName;
    @FXML
    private Label headTotal;
    @FXML
    private TableView<Item> itemTable;
    @FXML
    private TableView<OrderDetails> cartTable;
    @FXML
    private Label subTotal;
    @FXML
    private Label tax;
    @FXML
    private Label total;
    
    @FXML
    private TableColumn<OrderDetails, Integer> count;

    
    private ItemService itemService;
    private SaleService saleService;
    
    @FXML
    void clearCart() {
    	cartTable.getItems().clear();
    	calculate();
    }
    
    private Integer getData(ToIntFunction<OrderDetails> value) {
    	return cartTable.getItems().stream().mapToInt(value).sum();
    }
    
	private void calculate() {
    	subTotal.setText(getData(order->order.getSubTotal()).toString());
    	tax.setText(getData(od->od.getTax()).toString());
    	total.setText(getData(od->od.getTotal()).toString());
    }

    @FXML
    void clearSearch() {
    	category.setValue(null);
    	idName.clear();
    }

    @FXML
    void delete() {
    	try {
			OrderDetails order=cartTable.getSelectionModel().getSelectedItem();
			
			if (null != order) {
				cartTable.getItems().remove(order);
				calculate();
			} else {
				throw new PosException("Please select item in cart Table to delete !");
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageHandler.showAlert(e);
			MessageHandler.toFront();
		}
    }

    @FXML
    void paid() {
    	
    	try {
			if (cartTable.getItems().isEmpty()) {
				throw new PosException("Please select add item to cart !");
			}
			Voucher voucher = new Voucher();
			voucher.setSaleDate(LocalDate.now());
			voucher.setSaleTime(LocalTime.now());
			voucher.setId(voucher.getSaleTime().toSecondOfDay());
			
			voucher.getList().addAll(cartTable.getItems());
			
			saleService.paid(voucher);
			clearCart();
			
			MessageHandler.showAlert("Sieze: " + saleService.search(null, null, null, null).size() + "\nID " + voucher.getId());
		
			
		} catch (Exception e) {
			//e.printStackTrace();
			MessageHandler.showAlert(e);
		}
    	
    } 

    public void search() {
    	try {
    		System.out.printf("Category: %s%nID (or) Name: %s%n",
    				category.getValue(), idName.getText());
			List<Item> items = itemService.search(category.getValue(), idName.getText());
			itemTable.getItems().clear();
			itemTable.getItems().addAll(items);
			
			MainFrame.showMessage("Total Items : " + items.size());
			
		} catch (Exception e) {
			MessageHandler.showAlert(e);
			//e.printStackTrace();
		}
    }
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		itemService = ItemService.getInstance();
		saleService = SaleService.getInstance();
		
		category.getItems().addAll(Category.values());
		
		
		headTotal.textProperty().bind(total.textProperty());
		
		category.valueProperty().addListener( (a,b,c) -> search());
		idName.textProperty().addListener( (a,b,c) -> search());
		
		itemTable.setOnMouseClicked(event->{
			if(event.getClickCount() == 2) {
				Item item =itemTable.getSelectionModel().getSelectedItem();
				
				OrderDetails detail=cartTable.getItems().stream().filter(order ->order.getItem().getId()==item.getId()).findAny().orElse(null);
						
				
				if (detail==null) {
					detail=new OrderDetails();
					detail.setItem(item);
					cartTable.getItems().add(detail);
					
				}
				detail.setCount(detail.getCount()+1);
				detail.calculate();
				
				cartTable.refresh();
				
				calculate();
			}
		});
		
		//context menu
		MenuItem delete = new MenuItem("DELETE");
		delete.setOnAction(event ->delete());
		
		ContextMenu ctx= new ContextMenu(delete);
		cartTable.setContextMenu(ctx);
		
		search();
		
		//set editable table
		cartTable.setEditable(true);
		
		//converter for table-column
		count.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>() {

			@Override
			public Integer fromString(String arg0) {
				try {
					if (null != arg0 && !arg0.isEmpty() ) {
						return Integer.parseInt(arg0);
					}
				} catch (NumberFormatException e) {
				
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public String toString(Integer arg0) {
				if (null != arg0) {
					return arg0.toString();
				}
				return null;
			}
		}));
		
		//event after commit in table-column
		count.setOnEditCommit(event ->{
			
			OrderDetails detail=event.getRowValue();
			detail.setCount(event.getNewValue());
			detail.calculate();
			cartTable.refresh();
			calculate();
		});
	}

}
