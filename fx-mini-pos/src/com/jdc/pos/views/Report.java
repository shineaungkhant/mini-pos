package com.jdc.pos.views;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.jdc.pos.entities.Category;
import com.jdc.pos.entities.Item;
import com.jdc.pos.entities.OrderDetails;
import com.jdc.pos.service.ItemService;
import com.jdc.pos.service.SaleService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;


public class Report implements Initializable  {

    @FXML
    private ComboBox<Category> category;

    @FXML
    private ComboBox<Item> item;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private TableView<OrderDetails> table;

    private SaleService saleservice;
    private ItemService itenService;
    
    @FXML
    void clear() {
    	category.setValue(null);
    	item.setValue(null);
    	dateFrom.setValue(null);
    	dateTo.setValue(null);
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		saleservice =SaleService.getInstance();
		itenService=ItemService.getInstance();
		
		category.getItems().addAll(Category.values());
		category.valueProperty().addListener((a,b,c) -> {
			
			item.getItems().clear();
			
			Category cat=category.getValue();
			
			if (null != cat) {
				
			List<Item> list=itenService.search(cat, null); 
			item.getItems().addAll(list);
			
			}
			
			search();
			
		});
		
		item.valueProperty().addListener((a,b,c) -> search());
		dateFrom.valueProperty().addListener((a,b,c) -> search());
		dateTo.valueProperty().addListener((a,b,c) -> search());
		dateFrom.setValue(LocalDate.now());
		
	}

	private void search() {
		table.getItems().clear();
		List<OrderDetails> list=saleservice.search(category.getValue(), item.getValue(), 
				dateFrom.getValue(), dateTo.getValue());
		table.getItems().addAll(list);
		
	}
    
    

}
