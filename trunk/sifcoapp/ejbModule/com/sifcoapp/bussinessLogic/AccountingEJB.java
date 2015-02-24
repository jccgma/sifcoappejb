package com.sifcoapp.bussinessLogic;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.EJBException;
import javax.ejb.Stateless;

import com.sifcoapp.objects.accounting.dao.AccountingDAO;
import com.sifcoapp.objects.accounting.dao.JournalEntryDAO;
import com.sifcoapp.objects.accounting.dao.JournalEntryLinesDAO;
import com.sifcoapp.objects.accounting.to.AccPeriodTO;
import com.sifcoapp.objects.accounting.to.AccassignmentTO;
import com.sifcoapp.objects.accounting.to.AccountTO;
import com.sifcoapp.objects.accounting.to.JournalEntryInTO;
import com.sifcoapp.objects.accounting.to.JournalEntryLinesTO;
import com.sifcoapp.objects.accounting.to.JournalEntryTO;
import com.sifcoapp.objects.catalogos.Common;
import com.sifcoapp.objects.common.to.ResultOutTO;

/**
 * Session Bean implementation class AccountingEJB
 */
@Stateless
public class AccountingEJB implements AccountingEJBRemote {

	/**
	 * Default constructor.
	 */
	public AccountingEJB() {
		// TODO Auto-generated constructor stub
	}

	public List getAccPeriods() throws EJBException {
		List _return = new Vector();
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.getAccPeriods();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}

		return _return;
	}
	
	public List getAccount(int type) throws EJBException {
		List _return = new Vector();
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.getAccount(type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}

		return _return;
	}

	public int cat_accPeriod_mtto(int parameters, int usersign, int action) throws EJBException {

		int _return = 0;

		// Dividir el a�o en 12 periodos y crear objeto

		/*
		 * Agregar validadiones - Se haran desde la base - Que no este creado el
		 * a�o - Que el a�o sea mayor al actual
		 */
		AccountingDAO DAO = new AccountingDAO();
		//para el manejo de transacciones
		DAO.setIstransaccional(true);
		
		for (int i = 1; i <= 12; i++) {

			AccPeriodTO periodo = new AccPeriodTO();
			periodo.setAcccode(Integer.toString(i));
			periodo.setAccname(Integer.toString(parameters)
					+ String.format("%02d", i));
			periodo.setF_duedate(Common.getPrimerDiaDelMes(parameters, i));
			periodo.setF_refdate(Common.getPrimerDiaDelMes(parameters, i));
			periodo.setF_taxdate(Common.getPrimerDiaDelMes(parameters, i));
			periodo.setPeriodstat(1);
			periodo.setT_duedate(Common.getUltimoDiaDelMes(parameters, i));
			periodo.setT_refdate(Common.getUltimoDiaDelMes(parameters, i));
			periodo.setT_taxdate(Common.getUltimoDiaDelMes(parameters, i));
			periodo.setUsersign(usersign);
			try {
				_return = DAO.cat_accPeriod_mtto(periodo, action);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw (EJBException) new EJBException(e);
			}
		}
		
		DAO.forceCommit();
		DAO.forceCloseConnection();
		
		return _return;
	}

	public int cat_accAssignment_mtto(AccassignmentTO parameters, int action)throws EJBException {
		int _return = 0;

		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.cat_accAssignment_mtto(parameters, action);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public AccassignmentTO getAccAssignment() throws EJBException {
		AccassignmentTO _return = null;

		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.getAccAssignment();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public List getAccountByFilter(String acctcode, String acctname) throws EJBException {
		return  getAccountByFilter(acctcode, acctname, null);
	}

	public List getAccountByFilter(String acctcode, String acctname, String postable) throws EJBException {
		// TODO Auto-generated method stub
		List _return = new Vector();
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return= DAO.getAccountByFilter(acctcode, acctname,postable);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public AccountTO getAccountByKey(String acctcode)throws EJBException {
		// TODO Auto-generated method stub
		AccountTO acc= new AccountTO();
		AccountingDAO DAO= new AccountingDAO();
		try {
			acc = DAO.getAccountByKey(acctcode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return acc;
	}

	public int cat_acc0_ACCOUNT_mtto(AccountTO parameters, int action) throws EJBException {
		// TODO Auto-generated method stub
		int _return=0;
		AccountingDAO DAO= new AccountingDAO();
		try {
			_return= DAO.cat_acc0_ACCOUNT_mtto(parameters, action);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public List getTreeAccount() throws EJBException {
		// TODO Auto-generated method stub
		List _return= new Vector();
		AccountingDAO DAO= new AccountingDAO();
		try {
			_return=DAO.getTreeAccount();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}
	
//////###### journal entry####/////////////////////////////
	public List getJournalEntry(JournalEntryInTO parameters)
			throws EJBException {
		List _return= new Vector();
		JournalEntryDAO DAO= new JournalEntryDAO();
		try {
			_return= DAO.getJournalEntry(parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public JournalEntryTO getJournalEntryByKey(int transid) throws EJBException {
		JournalEntryTO _return = new JournalEntryTO();
		JournalEntryDAO DAO = new JournalEntryDAO();
		try {
			_return= DAO.getJournalEntryByKey(transid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public ResultOutTO journalEntry_mtto(JournalEntryTO parameters, int action) throws EJBException {
		//  VALOR POR DEFECTO PARA LOS DOUBLES##############
		double zero=0.00;
		ResultOutTO _return = new ResultOutTO();
		JournalEntryDAO DAO = new JournalEntryDAO();
		DAO.setIstransaccional(true);
		JournalEntryLinesDAO JournalLinesDAO = new JournalEntryLinesDAO(DAO.getConn());
		JournalLinesDAO.setIstransaccional(true);
		try {
			if(parameters.getLoctotal()==null){
				parameters.setLoctotal(zero);
			}
			if(parameters.getSystotal()==null){
				parameters.setSystotal(zero);
			}
			if(parameters.getTransrate()==null){
				parameters.setTransrate(zero);
			}
			if(parameters.getWtapplied()==null){
				parameters.setWtapplied(zero);
			}
			if(parameters.getBaseamnt()==null){
				parameters.setBaseamnt(zero);
			}
			if(parameters.getBasevtat()==null){
				parameters.setBasevtat(zero);
			}
			_return.setDocentry(DAO.journalEntry_mtto(parameters, action));
			_return.getDocentry();
		Iterator<JournalEntryLinesTO> iterator = parameters.getJournalentryList().iterator();
		while (iterator.hasNext()) {
			JournalEntryLinesTO Detalle = (JournalEntryLinesTO) iterator.next();
			if(Detalle.getDebit()==null){
				Detalle.setDebit(zero);
			}if(Detalle.getCredit()==null){
				Detalle.setCredit(zero);
			}if(Detalle.getTomthsum()==null){
				Detalle.setTomthsum(zero);
			}if(Detalle.getBasesum()==null){
				Detalle.setBasesum(zero);
			}if(Detalle.getVatrate()==null){
				Detalle.setVatrate(zero);
			}if(Detalle.getSysbasesum()==null){
				Detalle.setSysbasesum(zero);
			}if(Detalle.getVatamount()==null){
				Detalle.setVatamount(zero);
			}if(Detalle.getGrossvalue()==null){
				Detalle.setGrossvalue(zero);
			}if(Detalle.getBalduedeb()==null){
				Detalle.setBalduedeb(zero);
			}if(Detalle.getBalduecred()==null){
				Detalle.setBalduecred(zero);
			}if(Detalle.getTotalvat()==null){
				Detalle.setTotalvat(zero);
			}
			// Para articulos nuevos
			System.out.println("" + _return + "");
			Detalle.setTransid(_return.getDocentry());
			if (action == Common.MTTOINSERT) {
				JournalLinesDAO.journalEntryLines_mtto(Detalle,Common.MTTOINSERT);
			}
			if (action == Common.MTTODELETE) {
				JournalLinesDAO.journalEntryLines_mtto(Detalle,Common.MTTODELETE);
			}
		}
		DAO.forceCommit();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		DAO.rollBackConnection();
		throw (EJBException) new EJBException(e);
	} finally {

		DAO.forceCloseConnection();
		JournalLinesDAO.forceCloseConnection();
	}
	_return.setCodigoError(0);
	_return.setMensaje("Datos guardados con exito");
	return _return;
	}
	
	
	
}
