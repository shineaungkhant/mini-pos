package com.jdc.pos.service.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
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
		
		return null;
	}

}
