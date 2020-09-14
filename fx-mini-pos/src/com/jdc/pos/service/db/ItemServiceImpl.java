package com.jdc.pos.service.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jdc.pos.entities.Category;
import com.jdc.pos.entities.Item;
import com.jdc.pos.service.ItemService;
import com.jdc.pos.util.ConnectionManager;

public class ItemServiceImpl implements ItemService{
	
	private static final ItemService INSTANCE=new ItemServiceImpl();
	
	private static final String SELECT="select id, name, price, category from item where 1 = 1 ";
	
	private static final String INSERT="insert into item(id , name , category , price) value(?,?,?,?)";
	
	private ItemServiceImpl() {
		
	}
	
	public static ItemService getItemService() {
		return INSTANCE;
		
	}

	@Override
	public List<Item> search(Category c, String name) {
		//query
		StringBuilder query = new StringBuilder(SELECT);
		
		//param size
		List<Object> params = new ArrayList<Object>();
		
		List<Item> items= new ArrayList<Item>();
		
		if (null != c) {
			query.append(" and category = ?");
			params.add(c.toString());			
		}
		
		if (null != name && !name.isEmpty()) {
				if(check(name)) {
					query.append(" and id = ?");
					params.add(name);
				}else {
					
					query.append(" and name like ?");
					params.add(name.concat("%"));
				}
				
			
		}
			
		try (Connection conn=ConnectionManager.getConnection();
				PreparedStatement prep=conn.prepareStatement(query.toString())) {
			
			//set params
			for (int i = 0; i < params.size(); i++) {
				prep.setObject(i+1, params.get(i));
				
			}
			
			ResultSet rs = prep.executeQuery();
			
			while (rs.next()) {
			
				items.add(getItem(rs));
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return items;
	}

	

	private boolean check(String name) {
		try {
		 Integer.parseInt(name);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private Item getItem(ResultSet rs) throws SQLException{
		Item item=new Item();
		item.setCategory(Category.valueOf(rs.getString("category")));
		item.setId(rs.getInt("id"));
		item.setName(rs.getString("name"));
		item.setPrice(rs.getInt("price"));
		return item;
	}
	
	@Override
	public void add(Item item) {
		try(Connection connection=ConnectionManager.getConnection();
				PreparedStatement prep = connection.prepareStatement(INSERT)) {
			
			prep.setInt(1, item.getId());
			prep.setString(2, item.getName());
			prep.setString(3, item.getCategory().toString());
			prep.setInt(4, item.getPrice());
			
			prep.executeUpdate();
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void add(String path) {
	
		try {
			 Files.lines(Paths.get(path)).skip(1)
				.map(line -> line.split("\t"))
				.map(arr -> new Item(arr))
				.forEach(item->add(item));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
}
