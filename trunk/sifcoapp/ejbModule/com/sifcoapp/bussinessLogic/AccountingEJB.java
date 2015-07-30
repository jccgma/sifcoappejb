package com.sifcoapp.bussinessLogic;

import java.sql.Connection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.EJBException;
import javax.ejb.Stateless;

import org.glassfish.jersey.gf.ejb.internal.EjbExceptionMapper;

import com.sifcoapp.objects.accounting.dao.AccountingDAO;
import com.sifcoapp.objects.accounting.dao.JournalEntryDAO;
import com.sifcoapp.objects.accounting.dao.JournalEntryLinesDAO;
import com.sifcoapp.objects.accounting.to.AccPeriodTO;
import com.sifcoapp.objects.accounting.to.AccassignmentTO;
import com.sifcoapp.objects.accounting.to.AccountTO;
import com.sifcoapp.objects.accounting.to.BudgetTO;
import com.sifcoapp.objects.accounting.to.EntryTO;
import com.sifcoapp.objects.accounting.to.JournalEntryInTO;
import com.sifcoapp.objects.accounting.to.JournalEntryLinesInTO;
import com.sifcoapp.objects.accounting.to.JournalEntryLinesTO;
import com.sifcoapp.objects.accounting.to.JournalEntryTO;
import com.sifcoapp.objects.accounting.to.RecurringPostingsDetailTO;
import com.sifcoapp.objects.accounting.to.RecurringPostingsInTO;
import com.sifcoapp.objects.accounting.to.RecurringPostingsTO;
import com.sifcoapp.objects.admin.dao.AdminDAO;
import com.sifcoapp.objects.admin.dao.ParameterDAO;
import com.sifcoapp.objects.admin.to.ArticlesTO;
import com.sifcoapp.objects.admin.to.CatalogTO;
import com.sifcoapp.objects.admin.to.parameterTO;
import com.sifcoapp.objects.bank.to.ColecturiaDetailTO;
import com.sifcoapp.objects.bank.to.ColecturiaTO;
import com.sifcoapp.objects.catalog.dao.BusinesspartnerDAO;
import com.sifcoapp.objects.catalog.to.BusinesspartnerAcountTO;
import com.sifcoapp.objects.catalogos.Common;
import com.sifcoapp.objects.common.to.ResultOutTO;
import com.sifcoapp.objects.sales.to.ClientCrediDetailTO;

/**
 * Session Bean implementation class AccountingEJB
 */
@Stateless
public class AccountingEJB implements AccountingEJBRemote {
	Double zero = 0.00;

	/**
	 * Default constructor.
	 * 
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

	public ResultOutTO validate_exist_accperiod(Date parameters)
			throws EJBException {
		ResultOutTO _return = new ResultOutTO();
		AccountingDAO acc = new AccountingDAO();
		AccPeriodTO period = new AccPeriodTO();
		try {
			period = acc.validate_exist_accperiod(parameters);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			_return.setCodigoError(1);
			_return.setMensaje("no se encontro periodo contable");
			e.printStackTrace();
		}
		if (period.getPeriodstat() == 1) {
			_return.setCodigoError(0);
			_return.setMensaje("periodo contable activo ");
		} else {
			_return.setCodigoError(1);
			_return.setMensaje("periodo contable inactivo");
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

	public List getAccount_Toclose() throws EJBException {

		List _return = new Vector();
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.getAccount_Toclose();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}

		return _return;
	}

	public int cat_accPeriod_mtto(int parameters, int usersign, int action)
			throws EJBException {

		int _return = 0;

		// Dividir el a�o en 12 periodos y crear objeto

		/*
		 * Agregar validadiones - Se haran desde la base - Que no este creado el
		 * a�o - Que el a�o sea mayor al actual
		 */
		AccountingDAO DAO = new AccountingDAO();
		// para el manejo de transacciones
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

	public ResultOutTO cat_accAssignment_mtto(AccassignmentTO parameters,
			int action) throws EJBException {
		ResultOutTO _return = new ResultOutTO();

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

	public List getAccountByFilter(String acctcode, String acctname)
			throws EJBException {
		return getAccountByFilter(acctcode, acctname, null);
	}

	public List getAccountByFilter(String acctcode, String acctname,
			String postable) throws EJBException {
		// TODO Auto-generated method stub
		List _return = new Vector();
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.getAccountByFilter(acctcode, acctname, postable);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public AccountTO getAccountByKey(String acctcode) throws EJBException {
		// TODO Auto-generated method stub
		AccountTO acc = new AccountTO();
		AccountingDAO DAO = new AccountingDAO();
		try {
			acc = DAO.getAccountByKey(acctcode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return acc;
	}

	public ResultOutTO cat_acc0_ACCOUNT_mtto(AccountTO parameters, int action)
			throws EJBException {
		// TODO Auto-generated method stub
		ResultOutTO _return= new ResultOutTO();
		//int _return = 0;
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return.setDocentry(DAO.cat_acc0_ACCOUNT_mtto(parameters, action));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		
		return _return;
	}

	public int update_currtotal(AccountTO parameters, Connection conn)
			throws Exception {
		// TODO Auto-generated method stub
		int _return = 0;
		AccountingDAO DAO = new AccountingDAO(conn);
		DAO.setIstransaccional(true);

		_return = DAO.update_currtotal(parameters);

		return _return;
	}

	public List getTreeAccount() throws EJBException {
		// TODO Auto-generated method stub
		List _return = new Vector();
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.getTreeAccount();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	// ////###### journal entry####/////////////////////////////
	public List getJournalEntry(JournalEntryInTO parameters)
			throws EJBException {
		List _return = new Vector();
		JournalEntryDAO DAO = new JournalEntryDAO();
		try {
			_return = DAO.getJournalEntry(parameters);
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
			_return = DAO.getJournalEntryByKey(transid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public ResultOutTO journalEntry_mtto(JournalEntryTO parameters, int action) {
		ResultOutTO _return = new ResultOutTO();
		JournalEntryDAO DAO = new JournalEntryDAO();
		try {
			_return = journalEntry_mtto(parameters, action, DAO.getConn());
			DAO.forceCommit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			DAO.rollBackConnection();
			throw (EJBException) new EJBException(e);
		} finally {
			DAO.forceCloseConnection();
		}
		return _return;
	}

	public ResultOutTO journalEntry_mtto(JournalEntryTO parameters, int action,
			Connection _conn) throws Exception {

		JournalEntryDAO DAO = new JournalEntryDAO(_conn);
		double debe = zero;
		double haber = zero;
		ResultOutTO _return = new ResultOutTO();
		DAO.setIstransaccional(true);
		
		
		
		
		JournalEntryLinesDAO JournalLinesDAO = new JournalEntryLinesDAO(_conn);
		JournalLinesDAO.setIstransaccional(true);

		Iterator<JournalEntryLinesTO> iterator1 = parameters
				.getJournalentryList().iterator();
		while (iterator1.hasNext()) {
			JournalEntryLinesTO Detalle = (JournalEntryLinesTO) iterator1
					.next();

			if (Detalle.getDebit() != null) {
				debe = debe + Detalle.getDebit();
			}
			if (Detalle.getCredit() != null) {
				haber = haber + Detalle.getCredit();
			}

		}

		// validando que debe y haber sean iguales.....

	
		double valor = Math.abs(debe - haber);
		if (valor > 0.00001) {
			_return.setCodigoError(1);
			_return.setMensaje("No coincide los montos del Debe y Haber");
			throw new Exception("No coincide los montos del Debe y Haber");

		}

		// Valores por defecto
		double zero = 0.00;
		parameters.setObjtype("5");
		parameters.setBtfstatus("O");
		parameters.setRefndrprt("N");
		parameters.setAdjtran("N");
		parameters.setAutostorno("N");
		parameters.setAutovat("N");
		parameters.setPrinted("N");
		parameters.setAutowt("N");
		parameters.setDeferedtax("N");
		
		if(action==Common.MTTOINSERT){

		if (parameters.getLoctotal() == null) {
			parameters.setLoctotal(zero);
		}
		if (parameters.getSystotal() == null) {
			parameters.setSystotal(zero);
		}
		if (parameters.getTransrate() == null) {
			parameters.setTransrate(zero);
		}
		if (parameters.getWtapplied() == null) {
			parameters.setWtapplied(zero);
		}
		if (parameters.getBaseamnt() == null) {
			parameters.setBaseamnt(zero);
		}
		if (parameters.getBasevtat() == null) {
			parameters.setBasevtat(zero);
		}

		// Guardar encabezado

		_return.setDocentry(DAO.journalEntry_mtto(parameters, action));
	

		// Guardar detalle
		Iterator<JournalEntryLinesTO> iterator = parameters
				.getJournalentryList().iterator();
		while (iterator.hasNext()) {
			JournalEntryLinesTO Detalle = (JournalEntryLinesTO) iterator.next();

			// ------------------------------------------------------------------------------------------------------------
			// Valores por defecto del detalle
			// ------------------------------------------------------------------------------------------------------------

			AccountTO account = new AccountTO();

			Detalle.setReltransid(-1);
			Detalle.setRellineid(-1);
			Detalle.setReltype("N");
			Detalle.setObjtype("5");
			Detalle.setVatline("N");
			Detalle.setVatamount(0.0);
			Detalle.setClosed("N");
			Detalle.setGrossvalue(0.0);
			Detalle.setIsnet("Y");
			Detalle.setTaxtype(0);
			Detalle.setTaxpostacc("N");
			Detalle.setTotalvat(0.0);
			Detalle.setWtliable("N");
			Detalle.setWtline("N");
			Detalle.setPayblock("N");
			Detalle.setOrdered("N");

			if ((Detalle.getDebit() == null || Detalle.getDebit() == zero)
					&& (Detalle.getCredit() == null || Detalle.getCredit() == zero)) {
				throw new Exception("Error en valores");
			}

			if (Detalle.getDebit() == null || Detalle.getDebit() == zero) {
				Detalle.setDebit(zero);
				Detalle.setDebcred("C");
				account.setCurrtotal(Detalle.getCredit());

			} else {
				Detalle.setCredit(zero);
				Detalle.setDebcred("D");
				account.setCurrtotal(Detalle.getDebit());
			}
			/*
			 * if (Detalle.getCredit() == null) { Detalle.setCredit(zero); }
			 * else { Detalle.setDebcred("C");
			 * account.setCurrtotal(Detalle.getCredit()); }
			 */

			if (Detalle.getTomthsum() == null) {
				Detalle.setTomthsum(zero);
			}
			if (Detalle.getBasesum() == null) {
				Detalle.setBasesum(zero);
			}
			if (Detalle.getVatrate() == null) {
				Detalle.setVatrate(zero);
			}
			if (Detalle.getSysbasesum() == null) {
				Detalle.setSysbasesum(zero);
			}
			if (Detalle.getVatamount() == null) {
				Detalle.setVatamount(zero);
			}
			if (Detalle.getGrossvalue() == null) {
				Detalle.setGrossvalue(zero);
			}
			if (Detalle.getBalduedeb() == null) {
				Detalle.setBalduedeb(zero);
			}
			if (Detalle.getBalduecred() == null) {
				Detalle.setBalduecred(zero);
			}
			if (Detalle.getTotalvat() == null) {
				Detalle.setTotalvat(zero);
			}

			// ---------------------------------------------------------------------------------------------------------------
			Detalle.setTransid(_return.getDocentry());
			//Detalle.setTranstype(Integer.toString(_return.getDocentry()));
			if (action == Common.MTTOINSERT) {
				JournalLinesDAO.journalEntryLines_mtto(Detalle,
						Common.MTTOINSERT);
			}
			if (action == Common.MTTODELETE) {
				JournalLinesDAO.journalEntryLines_mtto(Detalle,
						Common.MTTODELETE);
			}

			// ---------------------------------------------------------------------------------------------------------------
			// actualizacion de saldo de cuenta
			// ---------------------------------------------------------------------------------------------------------------

			// account.setAcctcode(Detalle.getAccount());
			update_currtotal(Detalle, _conn);

		}
		}else{
			_return.setDocentry(DAO.journalEntry_mtto(parameters, Common.MTTOUPDATE));
			
		}
		_return.setCodigoError(0);
		_return.setMensaje("Datos guardados con exito");
		return _return;
	}

	public ResultOutTO update_currtotal(JournalEntryLinesTO line,
			Connection conn) throws Exception {
		ResultOutTO _return = new ResultOutTO();

		AccountTO account1 = new AccountTO();
		double saldo = 0.0;
		account1 = getAccountByKey(line.getAccount());

		if (account1 == null || account1.getPostable().equals("N")) {
			throw new Exception(
					"La cuenta contable no existe o no es posteable");
		}
		if (line.getDebcred().equals("C")) {
			saldo = account1.getCurrtotal() - line.getCredit();

		} else if (line.getDebcred().equals("D")) {
			saldo = account1.getCurrtotal() + line.getDebit();

		} else {
			throw new Exception(
					"Error en cuenta contable, informar al administrador");

		}

		account1.setCurrtotal(saldo);
		int i = update_currtotal(account1, conn);

		return _return;

	}

	

	// ################### BUDGET ######################
	public ResultOutTO cat_budget_mtto(BudgetTO parameters, int action)
			throws EJBException {

		ResultOutTO _return = new ResultOutTO();
		AccountingDAO DAO = new AccountingDAO();
		DAO.setIstransaccional(true);
		if (parameters.getCrdrltotal() == null) {
			parameters.setCrdrltotal(zero);
		}
		if (parameters.getCredltotal() == null) {
			parameters.setCredltotal(zero);
		}
		if (parameters.getDebltotal() == null) {
			parameters.setDebltotal(zero);
		}
		if (parameters.getDebrltotal() == null) {
			parameters.setDebrltotal(zero);
		}
		if (parameters.getFthrprcnt() == null) {
			parameters.setFthrprcnt(zero);
		}
		if (parameters.getFtridrlsum() == null) {
			parameters.setFtridrlsum(zero);
		}
		if (parameters.getFtridrssum() == null) {
			parameters.setFtridrssum(zero);
		}
		if (parameters.getFtrocrlsum() == null) {
			parameters.setFtrocrlsum(zero);
		}
		if (parameters.getFtrodrlsum() == null) {
			parameters.setFtrodrlsum(zero);
		}
		try {
			_return.setDocentry(DAO.cat_budget_mtto(parameters, action));
			DAO.forceCommit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			DAO.rollBackConnection();
			throw (EJBException) new EJBException(e);
		} finally {

			DAO.forceCloseConnection();
		}
		_return.setCodigoError(0);
		_return.setMensaje("Datos Ingresados con �xito");
		return _return;
	}

	public List getBudget(int _bgdcode) throws EJBException {
		List _return = new Vector();
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.getBudget(_bgdcode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	// ######################## RecurringPosting ####################
	public ResultOutTO fin_recurringPosting_mtto(
			RecurringPostingsTO parameters, int action) throws EJBException {
		// TODO Auto-generated method stub
		ResultOutTO _return = new ResultOutTO();
		List Detalles = new Vector();
		RecurringPostingsTO padre = new RecurringPostingsTO();
		AccountingDAO DAO = new AccountingDAO();
		DAO.setIstransaccional(true);
		if (parameters.getFinancvol() == null) {
			parameters.setFinancvol(zero);
		}
		if (parameters.getVolume() == null) {
			parameters.setVolume(zero);
		}
		try {
			// eliminar los detalles registrados antes de un update o en caso de
			// ser delete la accion
			if (action == Common.MTTOUPDATE || action == Common.MTTODELETE) {
				padre = DAO.getrecurringPosting_by_key(
						parameters.getRcurcode(), parameters.getInstance());

				Detalles = padre.getRecurringPostingsDetail();

				Iterator<RecurringPostingsDetailTO> iterator2 = Detalles
						.iterator();
				while (iterator2.hasNext()) {
					RecurringPostingsDetailTO Detallex = (RecurringPostingsDetailTO) iterator2
							.next();
					DAO.fin_recurringPostingDetail_mtto(Detallex,
							Common.MTTODELETE);
				}
			}
			// verificar la accion para obtener los hijos e insertarlos en ambos
			// casos (ya que se eliminan al principio si es un update)
			if (action == Common.MTTOINSERT || action == Common.MTTOUPDATE) {
				Iterator<RecurringPostingsDetailTO> iterator = parameters
						.getRecurringPostingsDetail().iterator();
				while (iterator.hasNext()) {
					RecurringPostingsDetailTO Detalle = (RecurringPostingsDetailTO) iterator
							.next();
					// Para articulos nuevos
					// System.out.println("" + _return + "");
					Detalle.setRcurcode(parameters.getRcurcode());
					if (Detalle.getCredit() == null) {
						Detalle.setCredit(zero);
					}
					if (Detalle.getDebit() == null) {
						Detalle.setDebit(zero);
					}
					if (Detalle.getGrossvalue() == null) {
						Detalle.setGrossvalue(zero);
					}
					if (action == Common.MTTOINSERT) {
						DAO.fin_recurringPostingDetail_mtto(Detalle,
								Common.MTTOINSERT);
					}
					if (action == Common.MTTOUPDATE) {
						DAO.fin_recurringPostingDetail_mtto(Detalle,
								Common.MTTOINSERT);
					}
				}
			}
			DAO.fin_recurringPosting_mtto(parameters, action);
			DAO.forceCommit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			DAO.rollBackConnection();
			throw (EJBException) new EJBException(e);
		} finally {

			DAO.forceCloseConnection();
		}
		_return.setCodigoError(0);
		_return.setMensaje("Datos Ingresados con �xito");
		return _return;
	}

	public List getrecurringPosting(RecurringPostingsInTO parameters)
			throws EJBException {
		// TODO Auto-generated method stub
		List _return = new Vector();
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.getrecurringPosting(parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public List getrecurringPosting_user(RecurringPostingsInTO parameters)
			throws EJBException {
		// TODO Auto-generated method stub
		List _return = new Vector();
		ParameterDAO parameter = new ParameterDAO();
		parameterTO param = new parameterTO();
		// cosulta en la tabla parametros para comparar si el usuario es
		// administrador
		try {
			param = parameter.getParameterbykey(8);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// si es administrador llama al metodo getrecurringPosting() para
		// mostrar todos los recurringPosting
		if (parameters.getUsersign() == param.getUsersign()) {

			_return = getrecurringPosting(parameters);
		} else {

			AccountingDAO DAO = new AccountingDAO();
			try {
				_return = DAO.getrecurringPosting_user(parameters);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw (EJBException) new EJBException(e);
			}
		}
		return _return;

	}

	public RecurringPostingsTO getrecurringPosting_by_key(String _rcurcode,
			int _instance) throws EJBException {
		// TODO Auto-generated method stub
		RecurringPostingsTO _return = new RecurringPostingsTO();
		AccountingDAO DAO = new AccountingDAO();
		try {
			_return = DAO.getrecurringPosting_by_key(_rcurcode, _instance);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public List getrecurringPostingExecute(int usersign) throws EJBException {
		// TODO Auto-generated method stub
		List _return = new Vector();
		AccountingDAO DAO = new AccountingDAO();
		try {

			_return = DAO.getrecurringPostingExecute(usersign);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public ResultOutTO saveTreeAccount(List parameters) throws EJBException {

		// TODO Auto-generated method stub
		ResultOutTO _return = new ResultOutTO();

		AccountingDAO DAO = new AccountingDAO();
		DAO.setIstransaccional(true);
		AccountTO node = new AccountTO();
		AccountTO account = new AccountTO();
		try {

			for (Object object : parameters) {
				AccountingDAO DAO1 = new AccountingDAO(DAO.getConn());
				DAO1.setIstransaccional(true);
				node = (AccountTO) object;
				if (!(node.getLevels() == 1)) {
					account = DAO1.getAccountByKey(node.getAcctcode());
					if (account.getAcctcode() != null) {

						int i = DAO1.account_mtto_new(node);
					} else {
						node.setCurrtotal(zero);
						node.setEndtotal(zero);
						int i = DAO1.cat_acc0_ACCOUNT_mtto(node,
								Common.MTTOINSERT);
					}

				}

			}
			DAO.forceCommit();
		} catch (Exception e) {
			// TODO: handle exception
			DAO.rollBackConnection();
			throw (EJBException) new EJBException(e);
		} finally {

			DAO.forceCloseConnection();
		}
		_return.setCodigoError(0);
		_return.setMensaje("Datos guardados correctamente");
		return _return;
	}

	public List getEntryDetail(JournalEntryLinesInTO parameters)
			throws Exception {
		List journal = new Vector();
		JournalEntryLinesTO nuevo = new JournalEntryLinesTO();
		JournalEntryLinesDAO DAO = new JournalEntryLinesDAO();
		JournalEntryLinesDAO DAO1 = new JournalEntryLinesDAO();

		// saldo inicial antes de la fecha indicada
		journal = DAO.getEntryDetail(parameters);
		EntryTO entrada = new EntryTO();
		entrada.setAcctcode(parameters.getAccount());
		entrada.setRefdate(parameters.getRefdate());

		nuevo = DAO1.getsaldo(entrada);

		double saldo = nuevo.getTotalvat();
		nuevo.setLinememo("Saldo inicial a la fecha");

		Iterator<JournalEntryLinesTO> iterator = journal.iterator();
		while (iterator.hasNext()) {
			JournalEntryLinesTO Detalle = (JournalEntryLinesTO) iterator.next();

			if (Detalle.getDebcred().equals("C")) {
				saldo = saldo - Detalle.getCredit();
				Detalle.setTotalvat(saldo);

			} else if (Detalle.getDebcred().equals("D")) {
				saldo = saldo + Detalle.getDebit();
				Detalle.setTotalvat(saldo);
			} else {
				throw new Exception(
						"Error en cuenta contable, informar al administrador");

			}
		}

		journal.add(0, nuevo);

		return journal;
	}

	public JournalEntryLinesTO getsaldo(EntryTO parameters) throws EJBException {
		JournalEntryLinesTO _return = new JournalEntryLinesTO();

		JournalEntryLinesDAO DAO = new JournalEntryLinesDAO();
		try {
			_return = DAO.getsaldo(parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		// para retornar el valor absoluto de saldo
		_return.setTotalvat(Math.abs(_return.getTotalvat()));
		return _return;
	}

	// -----------------------------------------------------------
	// elementos de prueba
	// -----------------------------------------------------------

	public JournalEntryTO getpruebaByKey(int transid) throws EJBException {
		JournalEntryTO _return = new JournalEntryTO();
		JournalEntryDAO DAO = new JournalEntryDAO();
		try {
			_return = DAO.getpruebaByKey(transid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public ResultOutTO journal_entry_new(JournalEntryTO parameters, int action)
			throws EJBException {

		JournalEntryDAO DAO = new JournalEntryDAO();

		DAO.setIstransaccional(true);
		JournalEntryLinesDAO JournalLinesDAO = new JournalEntryLinesDAO(
				DAO.getConn());
		JournalLinesDAO.setIstransaccional(true);

		// Valores por defecto
		double zero = 0.00;
		ResultOutTO _return = new ResultOutTO();
		try {

			if (parameters.getLoctotal() == null) {
				parameters.setLoctotal(zero);
			}
			if (parameters.getSystotal() == null) {
				parameters.setSystotal(zero);
			}
			if (parameters.getTransrate() == null) {
				parameters.setTransrate(zero);
			}
			if (parameters.getWtapplied() == null) {
				parameters.setWtapplied(zero);
			}
			if (parameters.getBaseamnt() == null) {
				parameters.setBaseamnt(zero);
			}
			if (parameters.getBasevtat() == null) {
				parameters.setBasevtat(zero);
			}

			// Guardar encabezado

			_return.setDocentry(DAO.journalEntry_mtto(parameters, action));

			_return.getDocentry();

			// Guardar detalle
			Iterator<JournalEntryLinesTO> iterator = parameters
					.getJournalentryList().iterator();
			while (iterator.hasNext()) {
				JournalEntryLinesTO Detalle = (JournalEntryLinesTO) iterator
						.next();

				// ------------------------------------------------------------------------------------------------------------
				// Valores por defecto del detalle
				// ------------------------------------------------------------------------------------------------------------

				AccountTO account = new AccountTO();

				if (Detalle.getDebit() == null) {
					Detalle.setDebit(zero);
				} else {
					Detalle.setDebcred("D");
					account.setCurrtotal(Detalle.getDebit());
				}
				if (Detalle.getCredit() == null) {
					Detalle.setCredit(zero);
				} else {
					Detalle.setDebcred("C");
					account.setCurrtotal(Detalle.getCredit());
				}
				if (Detalle.getTomthsum() == null) {
					Detalle.setTomthsum(zero);
				}
				if (Detalle.getBasesum() == null) {
					Detalle.setBasesum(zero);
				}
				if (Detalle.getVatrate() == null) {
					Detalle.setVatrate(zero);
				}
				if (Detalle.getSysbasesum() == null) {
					Detalle.setSysbasesum(zero);
				}
				if (Detalle.getVatamount() == null) {
					Detalle.setVatamount(zero);
				}
				if (Detalle.getGrossvalue() == null) {
					Detalle.setGrossvalue(zero);
				}
				if (Detalle.getBalduedeb() == null) {
					Detalle.setBalduedeb(zero);
				}
				if (Detalle.getBalduecred() == null) {
					Detalle.setBalduecred(zero);
				}
				if (Detalle.getTotalvat() == null) {
					Detalle.setTotalvat(zero);
				}

				// ---------------------------------------------------------------------------------------------------------------
				Detalle.setTransid(_return.getDocentry());
				
				if (action == Common.MTTOINSERT) {

					JournalLinesDAO.journalEntryLines_mtto(Detalle,
							Common.MTTOINSERT);

				}
				if (action == Common.MTTODELETE) {
					JournalLinesDAO.journalEntryLines_mtto(Detalle,
							Common.MTTODELETE);
				}

				// ---------------------------------------------------------------------------------------------------------------
				// actualizacion de saldo de cuenta
				// ---------------------------------------------------------------------------------------------------------------

				//account.setAcctcode(Detalle.getAccount());
				update_currtotal(Detalle, DAO.getConn());
				//update_currtotal(account, Detalle.getDebcred(), DAO.getConn());
			}
			DAO.forceCommit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			DAO.rollBackConnection();
			_return.setCodigoError(1);
			_return.setMensaje("error en almacenamiento de datos");
			return _return;

		} finally {

			DAO.forceCloseConnection();
		}

		_return.setCodigoError(0);
		_return.setMensaje("Datos guardados con exito");
		return _return;
	}

	public ResultOutTO fill_Journal_close(JournalEntryTO parameters)
			throws Exception {
		JournalEntryTO nuevo = new JournalEntryTO();
		ResultOutTO _result = new ResultOutTO();
		parameterTO cuenta = new parameterTO();
		String Objtype = "";
		boolean ind = false;
		Double total = zero;
		int n = 1;
		int count = 1;
		double total_sum = 0.0;
		List list_param = getAccount_Toclose();
		List aux = new Vector();
		List aux1 = new Vector();
		AdminDAO admin = new AdminDAO();

		ParameterDAO DAO = new ParameterDAO();
		cuenta = DAO.getParameterbykey(10);

		// recorre la lista de listas para encontrar los detalles de el asiento
		// contable
		List detail = new Vector();

		for (Object obj2 : list_param) {
			// encontramos la cuenta individual para ser comparada
			AccountTO account = (AccountTO) obj2;
			String linememo = " ";
			Double sum = zero;
			String acc = null;
			String pasivo = null;
			double ingreso;
			String caja;
			String business = null;
			JournalEntryLinesTO art1 = new JournalEntryLinesTO();
			JournalEntryLinesTO art2 = new JournalEntryLinesTO();

			total_sum = total_sum + Math.abs(account.getCurrtotal());

			// comparando con el group mask para encontrar el saldo de la cuenta
			if (account.getGroupmask() == 4) {

				art1.setCredit(account.getCurrtotal());
				art1.setBalduecred(account.getCurrtotal());
				art1.setBalduedeb(0.0);
				// cuenta perdidas y ganancias
				art2.setDebit(account.getCurrtotal());
				art2.setBalduedeb(account.getCurrtotal());
				art2.setBalduecred(0.0);

			}

			if (account.getGroupmask() == 5) {
				art1.setDebit(account.getCurrtotal() * -1);
				art1.setBalduecred(0.0);
				art1.setBalduedeb(account.getCurrtotal() * -1);
				// cuenta perdidas y ganancias
				art2.setCredit(account.getCurrtotal() * -1);
				art2.setBalduedeb(0.0);
				art2.setBalduecred(account.getCurrtotal() * -1);
			}

			art1.setLine_id(n);
			art1.setAccount(account.getAcctcode());

			// art1.setDuedate(parameters.getDocduedate());
			art1.setShortname(account.getAcctcode());
			art1.setContraact(cuenta.getValue1());
			art1.setLinememo("Asiento de cierre");
			// art1.setRefdate(parameters.getDocduedate());
			// art1.setRef1(parameters.getRef1());
			// art1.setRef2();
			// art1.setBaseref(parameters.getRef1());
			// art1.setTaxdate(parameters.getDocduedate());
			// art1.setFinncpriod(finncpriod);
			art1.setReltransid(-1);
			art1.setRellineid(-1);
			art1.setReltype("N");
			art1.setObjtype("5");
			art1.setVatline("N");
			art1.setVatamount(0.0);
			art1.setClosed("N");
			art1.setGrossvalue(0.0);
			art1.setIsnet("Y");
			art1.setTaxtype(0);
			art1.setTaxpostacc("N");
			art1.setTotalvat(0.0);
			art1.setWtliable("N");
			art1.setWtline("N");
			art1.setPayblock("N");
			art1.setOrdered("N");
			// art1.setTranstype(colecturia_c.getObjtype());
			detail.add(art1);

			n++;

			// ----------------------------------------------------------------------------------------------------------------------------------
			// cuenta de efectivo y caja
			// ----------------------------------------------------------------------------------------------------------------------------------

			art2.setLine_id(n);
			art2.setAccount(cuenta.getValue1());
			// art2.setDuedate(parameters.getDocduedate());
			art2.setShortname(cuenta.getValue1());
			art2.setContraact(account.getAcctcode());
			art2.setLinememo("asiento de cierre de periodo contable ");
			// art2.setRefdate();
			// art2.setRef1(parameters.getRef1());
			// art2.setRef2();
			// art2.setBaseref(parameters.getRef1());
			// art2.setTaxdate(parameters.getDocduedate());
			// art1.setFinncpriod(finncpriod);
			art2.setReltransid(-1);
			art2.setRellineid(-1);
			art2.setReltype("N");
			art2.setObjtype("5");
			art2.setVatline("N");
			art2.setVatamount(0.0);
			art2.setClosed("N");
			art2.setGrossvalue(0.0);
			art2.setIsnet("Y");
			art2.setTaxtype(0);
			art2.setTaxpostacc("N");
			art2.setTotalvat(0.0);
			art2.setWtliable("N");
			art2.setWtline("N");
			art2.setPayblock("N");
			art2.setOrdered("N");
			// art2.setTranstype(colecturia_c.getObjtype());
			n = n + 1;
			//
			detail.add(art2);

		}
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// llenado del asiento contable
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// LLenado del padre

		nuevo.setBtfstatus("O");
		nuevo.setTranstype(Objtype);
		nuevo.setBaseref("-1");
		// nuevo.setRefdate(parameters.getDocdate());
		nuevo.setMemo("asiento de cierre de periodo contable ");
		// nuevo.setRef1(Integer.toString(parameters.getDocnum()));
		// nuevo.setRef2(parameters.getRef1());
		nuevo.setLoctotal(total_sum);
		nuevo.setSystotal(total_sum);
		nuevo.setTransrate(0.0);
		// nuevo.setDuedate(parameters.getDocduedate());
		// nuevo.setTaxdate(parameters.getDocdate());
		nuevo.setFinncpriod(0);
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setRefndrprt("N");
		nuevo.setObjtype("5");
		nuevo.setAdjtran("N");
		nuevo.setAutostorno("N");
		nuevo.setSeries(0);
		nuevo.setAutovat("N");
		nuevo.setDocseries(0);
		nuevo.setPrinted("N");
		nuevo.setAutowt("N");
		nuevo.setDeferedtax("N");
		nuevo.setJournalentryList(detail);
		// ---------------------------------------------------------------------------------------------------------------------------------------------------------
		// metodo para reagrupar cuentas del mismo codigo
		// ---------------------------------------------------------------------------------------------------------------------------------------------------------
		JournalEntryTO journal = new JournalEntryTO();
		journal = fill_JournalEntry_Unir_Toclose(nuevo);
		// ---------------------------------------------------------------------------------------------------------------------------------------------------------
ResultOutTO _return= new ResultOutTO();

		_return=journalEntry_mtto(journal,Common.MTTOINSERT );
		
		return _return;

	}

	public JournalEntryTO fill_JournalEntry_Unir_Toclose(
			JournalEntryTO parameters) throws Exception {
		JournalEntryTO nuevo = new JournalEntryTO();
		ResultOutTO _result = new ResultOutTO();
		boolean ind = false;
		Double total = zero;
		Double sum_debe = 0.0;
		Double sum_credit = 0.0;
		int n = 1;
		// copiando la lista de los detalles de el asiento contable
		List list = parameters.getJournalentryList();
		// --------------------------------------------------------
		List aux = new Vector();
		List<List> listas = new Vector();
		List aux1 = new Vector();

		// recorre la lista de detalles
		for (Object obj : list) {
			ind = false;
			JournalEntryLinesTO good = (JournalEntryLinesTO) obj;
			String cod = good.getAccount();
			List lisHija = new Vector();

			// comparando lista aux de nodos visitados
			for (Object obj2 : aux) {
				JournalEntryLinesTO good2 = (JournalEntryLinesTO) obj2;
				if (cod.equals(good2.getAccount())) {
					ind = true;
				}
			}
			// compara el codigo de cuenta para hacer una sumatoria y guardarlo
			// en otra lista
			if (ind == false) {
				for (Object obj3 : list) {
					JournalEntryLinesTO good3 = (JournalEntryLinesTO) obj3;
					if (cod.equals(good3.getAccount())) {
						lisHija.add(good3);
					}
				}
				// guarda en la lista de listas
				listas.add(lisHija);
			}

			aux.add(good);

		}

		// recorre la lista de listas para encontrar los detalles de el asiento
		// contable
		List detail = new Vector();
		for (List obj1 : listas) {
			List listaDet = obj1;
			Double sum = zero;
			String acc = null;
			String c_acc = null;
			sum_debe = zero;
			sum_credit = zero;
			for (Object obj2 : listaDet) {
				JournalEntryLinesTO oldjournal = (JournalEntryLinesTO) obj2;
				if (oldjournal.getDebit() == null) {
					oldjournal.setDebit(0.0);
				}
				if (oldjournal.getCredit() == null) {
					oldjournal.setCredit(zero);
				}
				sum_debe = sum_debe + oldjournal.getDebit();
				sum_credit = sum_credit + oldjournal.getCredit();
				acc = oldjournal.getAccount();
				c_acc = oldjournal.getContraact();
			}

			// asiento contable

			JournalEntryLinesTO art1 = new JournalEntryLinesTO();
			// -----------------------------------------------------------------------------------
			// encontrando el saldo si es deudor o acreedor
			// -----------------------------------------------------------------------------------
			Double saldo = Math.abs(sum_debe) - Math.abs(sum_credit);
			if (saldo != 0) {
				if (saldo > 0) {
					art1.setDebit(saldo);
					art1.setBalduedeb(saldo);
					art1.setBalduecred(zero);
				} else {
					saldo = saldo * -1;
					art1.setCredit(saldo);
					art1.setBalduecred(saldo);
					art1.setBalduedeb(zero);
				}
				/*
				 * else { art1.setDebit(zero); art1.setBalduedeb(saldo);
				 * art1.setBalduecred(zero); }
				 */

				// --------------------------------------------------------------------------------------------------------------------------------------------------------
				// llenado del asiento contable
				// --------------------------------------------------------------------------------------------------------------------------------------------------------

				art1.setLine_id(n);
				art1.setAccount(acc);
				art1.setDuedate(parameters.getDuedate());
				art1.setShortname(acc);
				art1.setContraact(c_acc);
				art1.setLinememo("asiento de cierre de periodo contable");
				art1.setRefdate(parameters.getDuedate());
				art1.setRef1(parameters.getRef1());
				// art1.setRef2();
				art1.setBaseref(parameters.getRef1());
				art1.setTaxdate(parameters.getTaxdate());
				// art1.setFinncpriod(finncpriod);
				art1.setReltransid(-1);
				art1.setRellineid(-1);
				art1.setReltype("N");
				art1.setObjtype("5");
				art1.setVatline("N");
				art1.setVatamount(zero);
				art1.setClosed("N");
				art1.setGrossvalue(zero);
				art1.setIsnet("Y");
				art1.setTaxtype(0);
				art1.setTaxpostacc("N");
				art1.setTotalvat(0.0);
				art1.setWtliable("N");
				art1.setWtline("N");
				art1.setPayblock("N");
				art1.setOrdered("N");
				art1.setTranstype(parameters.getTranstype());
				detail.add(art1);
				n++;
			}
		}
		nuevo.setBtfstatus("O");
		nuevo.setTranstype(parameters.getTranstype());
		nuevo.setBaseref(parameters.getBaseref());
		nuevo.setRefdate(parameters.getRefdate());
		nuevo.setMemo(parameters.getMemo());
		nuevo.setRef1(parameters.getRef1());
		nuevo.setRef2(parameters.getRef2());
		nuevo.setLoctotal(parameters.getLoctotal());
		nuevo.setSystotal(parameters.getSystotal());
		nuevo.setTransrate(zero);
		nuevo.setDuedate(parameters.getDuedate());
		nuevo.setTaxdate(parameters.getTaxdate());
		nuevo.setFinncpriod(0);
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setRefndrprt("N");
		nuevo.setObjtype("5");
		nuevo.setAdjtran("N");
		nuevo.setAutostorno("N");
		nuevo.setSeries(0);
		nuevo.setAutovat("N");

		nuevo.setDocseries(0);
		nuevo.setPrinted("N");
		nuevo.setAutowt("N");
		nuevo.setDeferedtax("N");
		nuevo.setJournalentryList(detail);

		return nuevo;

	}

	public ResultOutTO Update_endTotal() throws EJBException {
		AccountingDAO DAO = new AccountingDAO();
		
		List accountlist = new Vector();
		ResultOutTO _return= new ResultOutTO();
		
		
			
				try {
					DAO.setIstransaccional(true);
					DAO.inicial_endTotal();
					
					DAO.update_TreeTotal();
					
					_return.setCodigoError(0);
					_return.setMensaje("datos almacenados correctamente");

					DAO.forceCommit();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					DAO.rollBackConnection();
					throw (EJBException) new EJBException(e);
				} finally {
					DAO.forceCloseConnection();
				}
		
		return _return;

	}
	
	public ResultOutTO traslado_caja(AccountTO account) throws Exception{
	
	double debe = 0.0;
	double haber = 0.0;
	double saldo = 0.0;
	
	JournalEntryLinesDAO DAO= new JournalEntryLinesDAO();
	
	ParameterDAO admin1 = new ParameterDAO();
	JournalEntryTO nuevo = new JournalEntryTO();
	ResultOutTO _result = new ResultOutTO();
	
	
	// lista de movimientos realizados por el usuario indicado 
	List list = DAO.getjournaldetail(account.getUsersing());
	List detail=new Vector();
	
	
	
	admin1 = new ParameterDAO();
	parameterTO Catalog1 = new parameterTO();
	Catalog1 = admin1.getParameterbykey(7);

	
	JournalEntryLinesTO art1 = new JournalEntryLinesTO();
	JournalEntryLinesTO art2 = new JournalEntryLinesTO(); 
	
	saldo=account.getCurrtotal();
	// Cuenta Caja.....
	
	if (saldo>0.00) {
		art1.setLine_id(1);
		//art1.setDebit(bussines);
		 art1.setCredit(saldo);
		art1.setAccount(Catalog1.getValue1());
		art1.setDuedate(account.getCreatedate());
		art1.setShortname(Catalog1.getValue1());
		art1.setContraact(account.getAcctcode());
		art1.setLinememo("traslado de caja a bancos");
		art1.setRefdate(account.getCreatedate());
		art1.setRef1(Integer.toString(account.getUsersing()));
		// ar1.setRef2();
		art1.setBaseref(art1.getRef1());
		art1.setTaxdate(account.getCreatedate());
		// art1.setFinncpriod(finncpriod);
		art1.setReltransid(-1);
		art1.setRellineid(-1);
		art1.setReltype("N");
		art1.setObjtype("5");
		art1.setVatline("N");
		art1.setVatamount(0.0);
		art1.setClosed("N");
		art1.setGrossvalue(0.0);
		art1.setBalduedeb(0.0);
		art1.setBalduecred(saldo);
		art1.setIsnet("Y");
		art1.setTaxtype(0);
		art1.setTaxpostacc("N");
		art1.setTotalvat(0.0);
		art1.setWtliable("N");
		art1.setWtline("N");
		art1.setPayblock("N");
		art1.setOrdered("N");
		//art1.setTranstype(parameters.getObjtype());
		detail.add(art1);
	}

	// cuenta Bancos..........
	
	art2.setLine_id(2);
	//art2.setCredit(bussines);
	 art2.setDebit(saldo);
	art2.setAccount(account.getAcctcode());
	art2.setDuedate(account.getCreatedate());
	art2.setShortname(account.getAcctcode());
	art2.setContraact(Catalog1.getValue1());
	art2.setLinememo("traslado de caja a bancos");
	art2.setRefdate(account.getCreatedate());
	art2.setRef1(Integer.toString(account.getUsersing()));
	// r1.setRef2();
	art2.setBaseref(art2.getRef1());
	art2.setTaxdate(account.getCreatedate());
	// art1.setFinncpriod(finncpriod);
	art2.setReltransid(-1);
	art2.setRellineid(-1);
	art2.setReltype("N");
	art2.setObjtype("5");
	art2.setVatline("N");
	art2.setVatamount(0.0);
	art2.setClosed("N");
	art2.setGrossvalue(0.0);
	art2.setBalduedeb(saldo);
	art2.setBalduecred(0.0);
	art2.setIsnet("Y");
	art2.setTaxtype(0);
	art2.setTaxpostacc("N");
	art2.setTotalvat(0.0);
	art2.setWtliable("N");
	art2.setWtline("N");
	art2.setPayblock("N");
	art2.setOrdered("N");
	//art2.setTranstype(parameters.getObjtype());
	detail.add(art2);

		
	
	
	// --------------------------------------------------------------------------------------------------------------------------------------------------------
	// llenado del asiento contable
	// --------------------------------------------------------------------------------------------------------------------------------------------------------
	// LLenado del padre
	
	nuevo.setObjtype("5");
	nuevo.setMemo("traslado de caja a Bancos");
	nuevo.setUsersign(account.getUsersing());
	nuevo.setLoctotal(saldo);
	nuevo.setSystotal(saldo);
	nuevo.setDuedate(account.getCreatedate());
	nuevo.setTaxdate(account.getCreatedate());
	nuevo.setBtfstatus("O");
	nuevo.setTranstype(nuevo.getObjtype());
	//nuevo.setBaseref(parameters.getRef1());
	nuevo.setRefdate(account.getCreatedate());
	//nuevo.setRef1(parameters.getRef1());
	nuevo.setRefndrprt("N");
	nuevo.setAdjtran("N");
	nuevo.setAutostorno("N");
	nuevo.setAutovat("N");
	nuevo.setPrinted("N");
	nuevo.setAutowt("N");
	nuevo.setDeferedtax("N");

	
	nuevo.setJournalentryList(detail);
	
	_result=journalEntry_mtto(nuevo, Common.MTTOINSERT);
	JournalEntryLinesDAO journal=new JournalEntryLinesDAO();
	int i=0;
	for (Object obj : list) {
		JournalEntryLinesTO good = (JournalEntryLinesTO) obj;
	good.setIntrnmatch(_result.getDocentry());
	good.setMthdate(account.getCreatedate());
	journal=new JournalEntryLinesDAO();
	i=journal.update_journalentryline(good);
		
	}
	
	_result.setCodigoError(0);
	_result.setDocentry(i);
	_result.setMensaje("Datos almacenados con exito");
	return _result;
	
}
    
	public Double devolver_saldo(AccountTO account)throws Exception{
		double _return=0.0;
		double debe = 0.0;
		double haber = 0.0;
		double saldo = 0.0;
		
		JournalEntryLinesDAO DAO= new JournalEntryLinesDAO();
		
		ParameterDAO admin1 = new ParameterDAO();
		JournalEntryTO nuevo = new JournalEntryTO();
		ResultOutTO _result = new ResultOutTO();
		
		
		// lista de movimientos realizados por el usuario indicado 
		List list = DAO.getjournaldetail(account.getUsersing());
		List detail=new Vector();
		
		
		
		admin1 = new ParameterDAO();
		parameterTO Catalog1 = new parameterTO();
		Catalog1 = admin1.getParameterbykey(7);

		for (Object obj : list) {
			JournalEntryLinesTO good = (JournalEntryLinesTO) obj;
			
			
	        debe=debe+good.getDebit();
	       
	        haber=haber+good.getCredit();
	        
			
		}
		JournalEntryLinesTO art1 = new JournalEntryLinesTO();
		JournalEntryLinesTO art2 = new JournalEntryLinesTO(); 
		
		saldo=debe-haber;
		
		if(saldo==0.00){
			_result.setCodigoError(1);
			_result.setMensaje("no se registran entradas de efectivo en caja");
			throw new Exception("no se registran entradas de efectivo en caja");
		}
	    
		if(saldo<0.00){
			_result.setCodigoError(1);
			_result.setMensaje("salidas de efectivos son mayores que las entradas");
			throw new Exception("salidas de efectivos son mayores que las entradas");

		}
		
		_return=Math.abs(saldo);
		return _return;
	}
	   
}
