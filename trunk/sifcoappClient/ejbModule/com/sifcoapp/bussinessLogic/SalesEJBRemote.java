package com.sifcoapp.bussinessLogic;

import javax.ejb.EJBException;
import javax.ejb.Remote;

@Remote
public interface SalesEJBRemote {
	public String doSales() throws EJBException;

}