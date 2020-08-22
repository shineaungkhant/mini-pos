package com.jdc.pos.service.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.jdc.pos.entities.Category;
import com.jdc.pos.entities.Item;
import com.jdc.pos.entities.OrderDetails;
import com.jdc.pos.entities.Voucher;
import com.jdc.pos.service.SaleService;



public class SaleServiceImpl implements SaleService {
		
	private static SaleService saleService =new SaleServiceImpl();
	private static final String FILE_NAME="sale.obj";
	
	private List<Voucher> vouchers;
	
	
	@SuppressWarnings("unchecked")
	SaleServiceImpl() {
		if(vouchers == null)
			vouchers = new ArrayList<Voucher>();
		
		try (ObjectInputStream input=new ObjectInputStream(
				new FileInputStream(FILE_NAME) )){
			
			vouchers=(List<Voucher>) input.readObject();
			
		} catch (Exception e) {
			System.err.println("First time loading .... ");
		}
	}
	
	
	public static SaleService getInstance() {
		return saleService;
		
	}
	
	@Override
	public void paid(Voucher voucher) {
		
		voucher.getList().forEach(od -> od.setVoucher(voucher));
		
		vouchers.add(voucher);
		saveFile(vouchers);

	}


	private void saveFile(List<Voucher> voucher) {
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(FILE_NAME))){
			
			out.writeObject(voucher);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


	@Override
	public List<OrderDetails> search(Category c, Item item, LocalDate dateFrom, LocalDate dateTo) {
		
		Predicate<OrderDetails> cond = a -> true;
		
		if(null != c) {
			cond =cond.and(detail -> detail.getItem().getCategory() == c);
		}
		
		if(null != item) {
			cond =cond.and(detail -> detail.getItem().equals(item));
			
			if(null != dateFrom) {
				cond = cond.and(detail -> detail.getVoucher().getSaleDate().compareTo(dateFrom) >= 0 );
			}
			
			if(null != dateTo) {
				cond = cond.and(detail -> detail.getVoucher().getSaleDate().compareTo(dateTo) <= 0 );
			}
		}
		
		return vouchers.stream()
				.flatMap(voucher -> voucher.getList().stream())
				.filter(cond)
				.collect(Collectors.toList());
	}

}
