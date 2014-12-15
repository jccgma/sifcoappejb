package com.sifcoapp.test;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import com.sifcoapp.client.AccountingEJBClient;
import com.sifcoapp.client.AdminEJBClient;
import com.sifcoapp.objects.admin.to.CatalogTO;
import com.sifcoapp.objects.admin.to.EnterpriseOutTO;
import com.sifcoapp.objects.admin.to.EnterpriseTO;
import com.sifcoapp.objects.admin.to.TablesCatalogTO;
import com.sifcoapp.objects.catalogos.Common;
import com.sifcoapp.objects.security.to.ProfileDetOutTO;

public class AdminTest {
	private static AdminEJBClient AdminEJBService;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if 		(AdminEJBService==null)
			AdminEJBService=new AdminEJBClient();
		
		String v_method=args[0];
		
		/*List lstPeriods=new Vector();
		
		lstPeriods=AccountingEJBService.getAccPeriods();
		
		System.out.println(lstPeriods);*/
		
		try {
			AdminTest.class.getMethod(args[0], null).invoke(null, null);
			//testPeriods();
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void saveEnterprise(){
		
		EnterpriseTO parameters=new EnterpriseTO();
		EnterpriseOutTO resp=null;
		
		parameters.setCode(1);
		parameters.setCompnyAddr("San Bartolo1");
		parameters.setCompnyName("ACOETMISAB");
		parameters.setCountry_catalog("01");
		parameters.setCrintHeadr("ACOETMISAB");
		parameters.setE_Mail("contactenos@acoetmisab.com.sv2");
		parameters.setFax("2222-1111");
		parameters.setManager("Pepe Perez");
		parameters.setPhone1("2222-2222");
		parameters.setPhone2("2222-3333");
		parameters.setTaxIdNum("1234-5");
		
		resp=AdminEJBService.saveEnterprise(parameters);
		
		System.out.println(resp.getRespCode());
		
	}
	
	public static void getEnterpriseInfo(){
		EnterpriseTO resp=null;
		
		resp=AdminEJBService.getEnterpriseInfo();
		
		System.out.println(resp.getCompnyName());
		System.out.println(resp.getCompnyAddr());
		
	}
	
	public static void findCatalog(){
		
		List catlgLst=null;
		
		catlgLst=AdminEJBService.findCatalog("paises");
		System.out.println("luego de servicio mod   ");
		Iterator<CatalogTO> iterator = catlgLst.iterator();
		while (iterator.hasNext()) {
			//System.out.println(iterator.next());
			CatalogTO catalogTO=(CatalogTO)iterator.next();
			System.out.println("--->"+ catalogTO.getCatcode() + "-"+ catalogTO.getCatvalue());
		
		}
	}
	/*
	 * Obtiene el catalogo de tablas del sistema
	 * */
	public static void getTablesCatalogTest(){
		
		List catlgLst=null;
		
		catlgLst=AdminEJBService.getTablesCatalog();
		System.out.println("luego de servicio");
		Iterator<TablesCatalogTO> iterator = catlgLst.iterator();
		while (iterator.hasNext()) {
			//System.out.println(iterator.next());
			TablesCatalogTO _returnTO=(TablesCatalogTO)iterator.next();
			System.out.println("Code: ->"+_returnTO.getCode());
			System.out.println("Name: ->"+_returnTO.getName());
		
		}
	}

	public static void cat_tab1_catalogos_mtto(){
	
		int _result;
		CatalogTO parameters = new CatalogTO();
				
		parameters.setTablecode(1);
		parameters.setCatcode("1");
		parameters.setCatvalue("Panama 2");
		parameters.setCatstatus("P");
		parameters.setUsersign(1);
		
		//Agregar
		
		//_result=AdminEJBService.cat_tab1_catalogos_mtto(parameters, Common.MTTOINSERT);
		
		//Actualizar
		
		parameters.setCatvalue("Honduras UPD");
		
		_result=AdminEJBService.cat_tab1_catalogos_mtto(parameters, Common.MTTOUPDATE);
		
		//Borrar
			
		//_result=AdminEJBService.cat_tab1_catalogos_mtto(parameters, Common.MTTODELETE);
		
		System.out.println("luego de servicio");
		System.out.println(_result);
		
	}
	
}