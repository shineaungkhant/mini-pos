package com.jdc.pos.service.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.jdc.pos.entities.Category;
import com.jdc.pos.entities.Item;
import com.jdc.pos.entities.OrderDetails;
import com.jdc.pos.entities.Voucher;
import com.jdc.pos.service.SaleService;
import com.jdc.pos.util.ConnectionManager;

public class SaleServiceImpl implements SaleService {
	
	private static final SaleService INSTANCE =new SaleServiceImpl();
	
	private static final String INSERT_VOUCHER = "insert into voucher (saleDate, saleTime) value(?,?)";
	
	private static final String INSERT_DETAIL = "insert into orderdetail (item , voucher, count, subTotal, tax, total  ) values (?,?,?,?,?,?)";
	
	private static final String SELECT = "select od.id detailId, v.id voucherId, i.id itemId,"
			+ " v.saleDate, v.saleTime, i.category, i.name, i.price,"
			+ " od.count, od.tax, od.subtotal, od.total "
			+ "from orderDetail od "
			+ "join voucher v "
			+ "on od.voucher = v.id"
			+ "join item i"
			+ " on od.item = i.id where 1 = 1";
	
	SaleServiceImpl(){
		
	}
	
	public static SaleService getInstance() {
		return INSTANCE;
		
	}
	
	@Override
	public void paid(Voucher voucher) {
		try(Connection conn = ConnectionManager.getConnection();
				PreparedStatement prepVoucher = conn.prepareStatement(INSERT_VOUCHER, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement prepDetail = conn.prepareStatement(INSERT_DETAIL)) {
			prepVoucher.setDate(1, Date.valueOf(LocalDate.now()));
			prepVoucher.setTime(2, Time.valueOf(LocalTime.now()));
			prepVoucher.executeUpdate();
			
			ResultSet rs= prepVoucher.getGeneratedKeys();
			
			while(rs.next()) {
				int voucherId =rs.getInt(1);
				
			for(OrderDetails detail : voucher.getList()) {
				prepDetail.setInt(1, detail.getItem().getId());
				prepDetail.setInt(2, voucherId);
				prepDetail.setInt(3, detail.getCount());
				prepDetail.setInt(4, detail.getSubTotal());
				prepDetail.setInt(5, detail.getTax());
				prepDetail.setInt(6, detail.getTotal());
				prepDetail.executeUpdate();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<OrderDetails> search(Category c, Item item, LocalDate dateFrom, LocalDate dateTo) {
		List<OrderDetails> list=new ArrayList<OrderDetails>();
		StringBuilder query=new StringBuilder(SELECT);
		List<Object> params=new ArrayList<Object>();
		if (null != c) {
			query.append(" and i.category = ?");
			params.add(c);
		}
		if (null != item) {
			query.append(" and i.name like ?");
			params.add(item.getName().concat("%"));
		}
		if (null != dateFrom) {
			query.append(" and v.saleDate >= ?");
			params.add(Date.valueOf(dateFrom));
		}
		if (null != dateTo) {
			query.append(" and v.saleDate <= ?");
			params.add(Date.valueOf(dateTo));
		}
		try(Connection conn = ConnectionManager.getConnection();
				PreparedStatement prep = conn.prepareStatement(query.toString())) {
			for (int i = 0; i < params.size(); i++) {
				prep.setObject(i+1, params.get(i));
			}
			
			ResultSet rs=prep.executeQuery();
			while(rs.next()) {
				Item i = new Item();
				i.setId(rs.getInt("itemId"));
				i.setName(rs.getString("name"));
				i.setCategory(Category.valueOf(rs.getString("category")));
				i.setPrice(rs.getInt("price"));
				
				Voucher v = new Voucher();
				v.setId(rs.getInt("voucherId"));
				v.setSaleDate(rs.getDate("saleDate").toLocalDate());
				v.setSaleTime(rs.getTime("saleTime").toLocalTime());
				
				OrderDetails detail=new OrderDetails();
				
				detail.setId(rs.getInt("detailId"));
				detail.setCount(rs.getInt("count"));
				detail.setSubTotal(rs.getInt("subtotal"));
				detail.setTax(rs.getInt("tax"));
				detail.setTotal(rs.getInt("total"));
				
				detail.setItem(i);
				detail.setVoucher(v);
				
				list.add(detail);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return list;
	}

}
