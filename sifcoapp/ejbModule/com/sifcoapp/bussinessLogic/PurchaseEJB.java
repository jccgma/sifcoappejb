package com.sifcoapp.bussinessLogic;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.EJBException;
import javax.ejb.Stateless;

import com.sifcoapp.admin.ejb.AdminEJB;
import com.sifcoapp.objects.accounting.to.AccountTO;
import com.sifcoapp.objects.accounting.to.JournalEntryLinesTO;
import com.sifcoapp.objects.accounting.to.JournalEntryTO;
import com.sifcoapp.objects.admin.dao.AdminDAO;
import com.sifcoapp.objects.admin.dao.ParameterDAO;
import com.sifcoapp.objects.admin.to.ArticlesTO;
import com.sifcoapp.objects.admin.to.BranchArticlesTO;
import com.sifcoapp.objects.admin.to.BranchTO;
import com.sifcoapp.objects.admin.to.CatalogTO;
import com.sifcoapp.objects.admin.to.parameterTO;
import com.sifcoapp.objects.catalog.dao.BusinesspartnerDAO;
import com.sifcoapp.objects.catalog.to.BusinesspartnerTO;
import com.sifcoapp.objects.catalogos.Common;
import com.sifcoapp.objects.common.to.ResultOutTO;
import com.sifcoapp.objects.inventory.dao.GoodReceiptDetailDAO;
import com.sifcoapp.objects.inventory.dao.GoodsReceiptDAO;
import com.sifcoapp.objects.inventory.to.GoodsReceiptDetailTO;
import com.sifcoapp.objects.inventory.to.GoodsreceiptTO;
import com.sifcoapp.objects.purchase.dao.*;
import com.sifcoapp.objects.purchase.to.*;
import com.sifcoapp.objects.sales.to.ClientCrediDetailTO;
import com.sifcoapp.objects.sales.to.ClientCrediTO;
import com.sifcoapp.objects.sales.to.DeliveryDetailTO;
import com.sifcoapp.objects.sales.to.SalesDetailTO;
import com.sifcoapp.objects.sales.to.SalesTO;
import com.sifcoapp.objects.transaction.dao.TransactionDAO;
import com.sifcoapp.objects.transaction.to.InventoryLogTO;
import com.sifcoapp.objects.transaction.to.TransactionTo;
import com.sifcoapp.objects.transaction.to.WarehouseJournalLayerTO;
import com.sifcoapp.objects.transaction.to.WarehouseJournalTO;
import com.sifcoapp.transaction.ejb.transactionEJB;

/**
 * Session Bean implementation class SalesEJB
 */
@Stateless
public class PurchaseEJB implements PurchaseEJBRemote {
	Double zero = 0.0;

	/**
	 * Default constructor.
	 */
	public PurchaseEJB() {
		// TODO Auto-generated constructor stub
	}

	// -----------------------------------------------------------------------------------------------------------------
	// mantenimientos compras
	// -----------------------------------------------------------------------------------------------------------------
	public ResultOutTO inv_Purchase_mtto(PurchaseTO parameters, int action)
			throws EJBException {

		// Declaración de variables

		ResultOutTO _valid = new ResultOutTO();
		ResultOutTO _return = new ResultOutTO();
		PurchaseDAO DAO = new PurchaseDAO();

		// --------------------------------------------------------------------------------------------------------------------------------
		// Validar acción a realizar
		// --------------------------------------------------------------------------------------------------------------------------------

		if (action != Common.MTTOINSERT) {
			_return = inv_Purchase_update(parameters, action);
			return _return;
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Asignación de valores por defecto y llenado:
		// Estas se realizan solo para cuando es guardar, el actualizar y borrar
		// no aplican.
		// --------------------------------------------------------------------------------------------------------------------------------
		parameters = fillPurchase(parameters);

		// --------------------------------------------------------------------------------------------------------------------------------
		// Hacer validaciones:
		// Estas se realizan solo para cuando es guardar, el actualizar y borrar
		// no aplican para validaciones
		// --------------------------------------------------------------------------------------------------------------------------------

		_valid = validate_Purchase(parameters);

		if (_valid.getCodigoError() != 0) {
			return _valid;
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Guardar en base:
		// Desde aqui se debe manajar como una transaccion global, cada metodo
		// debe tener una opción para ejecutarlo como parte de una transacción
		// global
		// --------------------------------------------------------------------------------------------------------------------------------

		try {
			DAO.setIstransaccional(true);
			_return = save_TransactionPurchase(parameters, action,
					DAO.getConn());
			DAO.forceCommit();

		} catch (Exception e) {
			DAO.rollBackConnection();
			throw (EJBException) new EJBException(e);
		} finally {

			DAO.forceCloseConnection();
		}

		_return.setCodigoError(0);
		_return.setMensaje("Datos guardados con exito");
		return _return;
	}

	public ResultOutTO inv_Purchase_update(PurchaseTO parameters, int action)
			throws EJBException {
		ResultOutTO _return = new ResultOutTO();
		PurchaseDAO DAO = new PurchaseDAO();
		try {
			_return = inv_Purchase_update(parameters, action, DAO.getConn());
			DAO.forceCommit();
		} catch (Exception e) {
			DAO.rollBackConnection();
			throw (EJBException) new EJBException(e);
		} finally {
			DAO.forceCloseConnection();
		}
		_return.setCodigoError(0);
		_return.setMensaje("Datos almacenados con exito");
		return _return;

	}

	public ResultOutTO inv_Purchase_update(PurchaseTO parameters, int action,
			Connection conn) throws Exception {
		// Variables
		ResultOutTO _return = new ResultOutTO();
		PurchaseDAO DAO = new PurchaseDAO(conn);
		DAO.setIstransaccional(true);
		PurchaseDetailDAO DAO1 = new PurchaseDetailDAO(conn);
		DAO1.setIstransaccional(true);

		// Actualizar/borrar encabezados
		_return.setDocentry(DAO.inv_Purchase_mtto(parameters, action));

		// Borrar detalles
		Iterator<PurchaseDetailTO> iterator = parameters.getpurchaseDetails()
				.iterator();
		if (action == Common.MTTODELETE) {
			while (iterator.hasNext()) {
				PurchaseDetailTO detalleReceipt = (PurchaseDetailTO) iterator
						.next();

				DAO1.inv_PurchaseDetail_mtto(detalleReceipt, Common.MTTODELETE);
			}
		}

		_return.setCodigoError(0);
		_return.setMensaje("Datos Actualizados con exito");
		return _return;
	}

	private PurchaseTO fillPurchase(PurchaseTO parameters) {

		// variables
		Double total = zero;
		Double vatsum = zero;
		ArticlesTO DBArticle = new ArticlesTO();
		AdminDAO admin = new AdminDAO();

		BranchTO branch1 = new BranchTO();
		// buscando la cuenta asignada de cuenta de existencias al almacen

		try {
			branch1 = admin.getBranchByKey(parameters.getTowhscode());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String branch_c = branch1.getBalinvntac();
		// --------------------------------------------------------------------------------------------------------------------------------
		// Valores por defecto detalle
		// --------------------------------------------------------------------------------------------------------------------------------
		@SuppressWarnings("unchecked")
		Iterator<PurchaseDetailTO> iterator = parameters.getpurchaseDetails()
				.iterator();

		while (iterator.hasNext()) {
			PurchaseDetailTO articleDetalle = (PurchaseDetailTO) iterator
					.next();

			AdminEJB EJB = new AdminEJB();
			DBArticle = EJB.getArticlesByKey(articleDetalle.getItemcode());

			// Asignar a documento
			articleDetalle.setArticle(DBArticle);
			articleDetalle.setAcctcode(branch_c);
			// Valores por defecto
			articleDetalle.setDocentry(parameters.getDocentry());
			articleDetalle.setTargettype(-1);
			// articleDetalle.setBaseref("");
			articleDetalle.setBasetype(-1);
			articleDetalle.setLinestatus("O");
			articleDetalle.setDscription(DBArticle.getItemName());
			articleDetalle.setDiscprcnt(zero);
			articleDetalle.setWhscode(parameters.getTowhscode());
			// articleDetalle.setAcctcode(acctcode);
			articleDetalle.setTaxstatus("Y");
			articleDetalle.setOcrcode(parameters.getTowhscode());
			articleDetalle.setFactor1(zero);
			articleDetalle.setObjtype("20");
			articleDetalle.setGrssprofit(articleDetalle.getGrssprofit());
			articleDetalle.setVatappld(articleDetalle.getVatappld());
			articleDetalle.setUnitmsr(DBArticle.getBuyUnitMsr());
			articleDetalle.setStockpricestockprice(articleDetalle
					.getStockpricestockprice());

			// Calculo de impuesto
			vatsum = vatsum + articleDetalle.getVatsum();

			// Calculo de totales
			articleDetalle.setLinetotal(articleDetalle.getQuantity()
					* articleDetalle.getPrice());
			articleDetalle.setOpenqty(articleDetalle.getQuantity());
			total = total + articleDetalle.getGtotal();

		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Valores por defecto encabezado
		// --------------------------------------------------------------------------------------------------------------------------------
		parameters.setDocnum(parameters.getDocentry());
		parameters.setDoctype("I");
		parameters.setCanceled("N");
		parameters.setDocstatus("O");
		parameters.setObjtype("20");
		parameters.setVatsum(vatsum);
		parameters.setDiscsum(zero);
		parameters.setDoctotal(total);
		parameters.setRef1(Integer.toString(parameters.getDocnum()));
		parameters.setJrnlmemo("Facturas de proveedores - "
				+ parameters.getCardcode());
		parameters.setReceiptnum(0);
		parameters.setGroupnum(0);
		parameters.setConfirmed("Y");
		parameters.setCreatetran("Y");
		// parameters.setSeries(0);
		parameters.setRounddif(zero);
		parameters.setRounding("N");
		// parameters.setCtlaccount(ctlaccount); Aqui deberia de hacerse la
		// consulta he incluirse la cuenta contables
		parameters.setPaidsum(zero);
		parameters.setNret(zero);
		parameters.setObjtype("20");
		return parameters;
	}

	public ResultOutTO validate_Purchase(PurchaseTO parameters)
			throws EJBException {
		boolean valid = false;
		ResultOutTO _return = new ResultOutTO();
		AccountingEJB acc = new AccountingEJB();
		CatalogEJB Businesspartner = new CatalogEJB();
		AdminEJB EJB1 = new AdminEJB();
		List branch = new Vector();
		ArticlesTO DBArticle = new ArticlesTO();
		String code;

		// ------------------------------------------------------------------------------------------------------------
		// validaciones
		// ------------------------------------------------------------------------------------------------------------

		// ------------------------------------------------------------------------------------------------------------
		// Validación almacen bloqueado
		// ------------------------------------------------------------------------------------------------------------
		if (parameters.getTowhscode() == null) {
			_return.setCodigoError(1);
			_return.setMensaje("Codigo de almacen null");

			return _return;
		}
		_return = EJB1.validate_branchActiv(parameters.getTowhscode());

		if (_return.getCodigoError() != 0) {
			_return.setCodigoError(1);
			_return.setMensaje("El Almacen no esta activo");

			return _return;
		}
		// ------------------------------------------------------------------------------------------------------------
		// Validación de fecha de periodo contable
		// ------------------------------------------------------------------------------------------------------------
		if (parameters.getDocdate() == null) {
			_return.setCodigoError(1);
			_return.setMensaje("no se encuentra fecha del documento ");

			return _return;
		}
		_return = acc.validate_exist_accperiod(parameters.getDocdate());
		if (_return.getCodigoError() != 0) {
			_return.setCodigoError(1);
			_return.setMensaje("El documento tiene una fecha Fuera del periodo contable activo");
			return _return;
		}
		// ------------------------------------------------------------------------------------------------------------
		// Validación del socio de negocio
		// ------------------------------------------------------------------------------------------------------------
		if (parameters.getCardcode() == null) {
			_return.setCodigoError(1);
			_return.setMensaje("Codigo de almacen null");

			return _return;
		}
		_return = Businesspartner.validate_businesspartnerBykey(parameters
				.getCardcode());
		if (_return.getCodigoError() != 0) {
			_return.setCodigoError(1);
			_return.setMensaje("el socio de negocio no esta activo para esta transaccion");
			return _return;
		}

		Iterator<PurchaseDetailTO> iterator1 = parameters.getpurchaseDetails()
				.iterator();

		// recorre el ClientCrediDetail
		while (iterator1.hasNext()) {

			// Consultar información actualizada desde la base
			PurchaseDetailTO PurchaseDetail = (PurchaseDetailTO) iterator1
					.next();

			DBArticle = PurchaseDetail.getArticle();

			// ------------------------------------------------------------------------------------------------------------
			// Validación articulo existe
			// ------------------------------------------------------------------------------------------------------------
			valid = false;
			if (DBArticle != null) {
				valid = true;
			}

			if (!valid) {
				_return.setLinenum(PurchaseDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ PurchaseDetail.getItemcode() + " "
						+ PurchaseDetail.getDscription()

						+ " no existe,informar al administrador. linea :"
						+ PurchaseDetail.getLinenum());

				return _return;

			}

			// ------------------------------------------------------------------------------------------------------------
			// Validación articulo activo
			// ------------------------------------------------------------------------------------------------------------

			valid = false;
			if (DBArticle.getValidFor() != null
					&& DBArticle.getValidFor().toUpperCase().equals("Y")) {
				valid = true;
			}

			if (!valid) {
				_return.setLinenum(PurchaseDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ PurchaseDetail.getItemcode() + " "
						+ PurchaseDetail.getDscription()

						+ " No esta activo. linea :"
						+ PurchaseDetail.getLinenum());

				return _return;

			}

			// ------------------------------------------------------------------------------------------------------------
			// Validación articulo de compra
			// ------------------------------------------------------------------------------------------------------------
			valid = false;
			if (DBArticle.getPrchseItem() != null
					&& DBArticle.getPrchseItem().toUpperCase().equals("Y")) {
				valid = true;
			}

			if (!valid) {
				_return.setLinenum(PurchaseDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ PurchaseDetail.getItemcode() + " "
						+ PurchaseDetail.getDscription()
						+ " No es un articulo de venta. linea :"
						+ PurchaseDetail.getLinenum());
				return _return;
			}

			// ------------------------------------------------------------------------------------------------------------
			// Validación almacen bloqueado para articulo
			// ------------------------------------------------------------------------------------------------------------
			valid = false;

			branch = DBArticle.getBranchArticles();

			for (Object object : branch) {
				BranchArticlesTO branch1 = (BranchArticlesTO) object;

				if (branch1.getWhscode().equals(PurchaseDetail.getWhscode())) {
					if (branch1.isIsasociated() && branch1.getLocked() != null
							&& branch1.getLocked().toUpperCase().equals("F")) {
						valid = true;
					}
				}
			}

			if (!valid) {
				_return.setLinenum(PurchaseDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ PurchaseDetail.getItemcode()
						+ " "
						+ PurchaseDetail.getDscription()
						+ " No esta asignado o esta bloquedo para el almacen indicado. linea :"
						+ PurchaseDetail.getLinenum());
				return _return;
			}

		}
		_return.setCodigoError(0);

		return _return;

	}

	public ResultOutTO save_TransactionPurchase(PurchaseTO purchase,
			int action, Connection conn) throws Exception {

		// Variables
		List transactions = new Vector();
		InventoryLogTO inventoryLog = new InventoryLogTO();
		WarehouseJournalTO warehouseJournal = new WarehouseJournalTO();
		WarehouseJournalLayerTO warehouseJournallayer = new WarehouseJournalLayerTO();
		ResultOutTO _return = new ResultOutTO();
		ResultOutTO res_invet = new ResultOutTO();
		ResultOutTO res_whj = new ResultOutTO();
		ResultOutTO res_whjl = new ResultOutTO();
		ResultOutTO res_jour = new ResultOutTO();
		ResultOutTO res_UpdateOnhand = new ResultOutTO();
		PurchaseDAO DAO = new PurchaseDAO(conn);
		transactionEJB trans = new transactionEJB();
		JournalEntryTO journal = new JournalEntryTO();

		// --------------------------------------------------------------------------------------------------------------------------------
		// Guardar Encabezados y detalles de Entrada
		// --------------------------------------------------------------------------------------------------------------------------------

		_return = inv_purchase_save(purchase, action, conn);
		purchase.setDocentry(_return.getDocentry());
		purchase.setDocnum(_return.getDocentry());
		purchase.setRef1(Integer.toString(purchase.getDocnum()));

		// --------------------------------------------------------------------------------------------------------------------------------
		// Llenar objeto tipo transacción
		// --------------------------------------------------------------------------------------------------------------------------------

		transactions = fill_transaction(purchase);

		// --------------------------------------------------------------------------------------------------------------------------------
		// Calculo de existencias y costos
		// --------------------------------------------------------------------------------------------------------------------------------
		for (Object object : transactions) {
			TransactionTo ivt = (TransactionTo) object;
			ivt = trans.calculate(ivt);
			TransactionDAO transDAO = new TransactionDAO(conn);
			transDAO.setIstransaccional(true);
			res_UpdateOnhand = transDAO.Update_Onhand_articles(ivt);
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Registro de Inventory Log
		// --------------------------------------------------------------------------------------------------------------------------------

		for (Object object : transactions) {
			TransactionTo ivt = (TransactionTo) object;

			inventoryLog = trans.fill_Inventory_Log(ivt);
			res_invet = trans.save_Inventory_Log(inventoryLog, conn);
			// Asignación de codigo MessageId
			ivt.setMessageid(res_invet.getDocentry());
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Registro de Warehouse Journal
		// --------------------------------------------------------------------------------------------------------------------------------

		for (Object object : transactions) {
			TransactionTo ivt = (TransactionTo) object;
			warehouseJournal = trans.fill_WarehouseJournal(ivt);
			// Asiganción de TransSeq
			res_whj = trans.save_WarehouseJournal(warehouseJournal, conn);
			ivt.setTransseq(res_whj.getDocentry());
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Registro de Warehouse Journal layer
		// --------------------------------------------------------------------------------------------------------------------------------

		for (Object object : transactions) {

			TransactionTo ivt = (TransactionTo) object;
			warehouseJournallayer = trans.fill_WarehouseJournalLayer(ivt);
			// Asiganción de TransSeq
			res_whjl = trans.save_WarehouseJournalLayer(warehouseJournallayer,
					conn);
		}

		// -----------------------------------------------------------------------------------
		// registro del asiento contable y actualización de saldos
		// -----------------------------------------------------------------------------------

		journal = fill_JournalEntry(purchase);

		AccountingEJB account = new AccountingEJB();
		res_jour = account.journalEntry_mtto(journal, Common.MTTOINSERT, conn);

		// -----------------------------------------------------------------------------------
		// Actualización de documento con datos de Asiento contable
		// -----------------------------------------------------------------------------------

		purchase.setTransid(res_jour.getDocentry());
		_return = inv_Purchase_update(purchase, Common.MTTOUPDATE, conn);

		if (purchase.getPeymethod().equals("1")) {
			journal = fill_JournalEntry_pago(purchase);

			AccountingEJB account1 = new AccountingEJB();
			res_jour = account1.journalEntry_mtto(journal, Common.MTTOINSERT,
					conn);
			// -----------------------------------------------------------------------------------
			// Actualización de documento con datos de Asiento contable
			// -----------------------------------------------------------------------------------
			purchase.setDocstatus("C");
			purchase.setPaidtodate(purchase.getDocdate());
			purchase.setPaidsum(journal.getSystotal());
			_return = inv_Purchase_update(purchase, Common.MTTOUPDATE, conn);
		}

		return _return;
	}

	private List fill_transaction(PurchaseTO document) {
		List _return = new Vector();

		Iterator<PurchaseDetailTO> iterator = document.getpurchaseDetails()
				.iterator();
		while (iterator.hasNext()) {
			PurchaseDetailTO detail = (PurchaseDetailTO) iterator.next();

			TransactionTo transaction = new TransactionTo();
			transaction.setTransseq(0);
			transaction.setDocentry(document.getDocentry());
			transaction.setDocnum(Integer.toString(document.getDocnum()));
			transaction.setDocduedate(document.getDocduedate());
			transaction.setDocdate(document.getDocdate());
			transaction.setComment(document.getComments());
			transaction.setJrnlmemo(document.getJrnlmemo());
			transaction.setUsersign(document.getUsersign());
			transaction.setRef1(Integer.toString(document.getDocnum()));
			transaction.setRef2(document.getNumatcard());
			transaction.setLinenum(detail.getLinenum());
			transaction.setItemcode(detail.getItemcode());
			transaction.setDscription(detail.getDscription());
			transaction.setQuantity(detail.getQuantity());
			transaction.setPrice(detail.getPrice());
			transaction.setLinetotal(detail.getLinetotal());
			transaction.setWhscode(detail.getWhscode());
			transaction.setAcctcode(detail.getAcctcode());
			transaction.setOcrcode(document.getCardcode());
			transaction.setVatgroup(detail.getVatgroup());
			transaction.setPriceafvat(detail.getPriceafvat());
			transaction.setVatsum(detail.getVatsum());
			transaction.setObjtype(detail.getObjtype());
			transaction.setGrssprofit(detail.getGrssprofit());
			transaction.setTaxcode(detail.getTaxcode());
			transaction.setVatappld(detail.getVatappld());
			transaction.setStockprice(detail.getPrice());
			transaction.setGtotal(detail.getGtotal());
			transaction.setInqty(zero);
			transaction.setOutqty(zero);
			transaction.setMessageid(0);
			transaction.setBalance(zero);
			transaction.setNewOnhand(zero);
			transaction.setNewWhsOnhand(zero);
			transaction.setNewAvgprice(zero);
			transaction.setArticle(detail.getArticle());

			_return.add(transaction);

		}
		return _return;
	}

	public ResultOutTO inv_purchase_save(PurchaseTO parameters, int action,
			Connection conn) throws Exception {
		// Variables
		ResultOutTO _return = new ResultOutTO();
		PurchaseDAO DAO = new PurchaseDAO(conn);
		DAO.setIstransaccional(true);
		PurchaseDetailDAO goodDAO1 = new PurchaseDetailDAO(conn);
		goodDAO1.setIstransaccional(true);

		// --------------------------------------------------------------------------------------------------------------------------------
		// Guardar encabezados
		// --------------------------------------------------------------------------------------------------------------------------------

		_return.setDocentry(DAO.inv_Purchase_mtto(parameters, action));

		// --------------------------------------------------------------------------------------------------------------------------------
		// Guardar detalles
		// --------------------------------------------------------------------------------------------------------------------------------

		Iterator<PurchaseDetailTO> iterator = parameters.getpurchaseDetails()
				.iterator();
		while (iterator.hasNext()) {
			PurchaseDetailTO detalleReceipt = (PurchaseDetailTO) iterator
					.next();

			detalleReceipt.setDocentry(_return.getDocentry());
			goodDAO1.inv_PurchaseDetail_mtto(detalleReceipt, Common.MTTOINSERT);

		}

		_return.setCodigoError(0);
		_return.setMensaje("Datos guardados con exito");
		return _return;
	}

	public JournalEntryTO fill_JournalEntry(PurchaseTO parameters)
			throws Exception {

		String buss_c;
		String branch_c;
		String iva_c = null;
		String V_local;
		String costo_venta;
		String fovial = null;
		String cotrans_C = null;
		double bussines = 0.0;
		double branch = 0.0;
		double tax = 0.0;
		double fovc = 0.0;
		double cotrans = 0.0;
		double sale = 0.0;
		double costo = 0.0;
		double impuesto = 0.0;

		JournalEntryTO nuevo = new JournalEntryTO();
		ResultOutTO _result = new ResultOutTO();
		boolean ind = false;
		Double total = zero;
		List list = parameters.getpurchaseDetails();
		List aux = new Vector();
		List<List> listas = new Vector();
		List aux1 = new Vector();
		// recorre la lista de detalles

		ArticlesTO arti = new ArticlesTO();

		for (Object obj : list) {
			PurchaseDetailTO good = (PurchaseDetailTO) obj;
			String cod = good.getAcctcode();
			List lisHija = new Vector();
			CatalogTO Catalog = new CatalogTO();
			AdminDAO admin = new AdminDAO();
			Catalog = admin.findCatalogByKey(good.getTaxcode(), 12);
			// calculando los impuestos y saldo de las cuentas
			// --------------------------------------------------------------------------------

			arti = good.getArticle();
			branch = branch + (good.getPrice() * good.getQuantity());
			// sale = sale + good.getLinetotal();
			// calculando el iva validando si el producto esta exento o de iva
			if (good.getTaxstatus().equals("Y")) {
				// validar si es FOV
				if (good.getTaxcode().equals("FOV")) {

					admin = new AdminDAO();
					CatalogTO Catalog1 = new CatalogTO();
					Catalog1 = admin.findCatalogByKey("FOV1", 12);
					if (Catalog1.getCatvalue3() == null) {
						throw new Exception(
								"No tiene cuenta asignada para  impuestos");
					}
					if (Catalog1.getCatvalue2() == null) {
						throw new Exception(
								"No tiene cuenta asignada para  impuestos");
					}
					if (Catalog1.getCatvalue() == null) {
						throw new Exception(
								"No tiene cuenta asignada para  impuestos");
					}
					cotrans_C = Catalog1.getCatvalue3();
					fovial = Catalog1.getCatvalue2();
					iva_c = Catalog1.getCatvalue();

					impuesto = good.getLinetotal()
							* (Double.parseDouble(Catalog.getCatvalue()) / 100);
					fovc = fovc
							+ (Double.parseDouble(Catalog.getCatvalue2()) * good
									.getQuantity());
					cotrans = cotrans
							+ (Double.parseDouble(Catalog.getCatvalue3()) * good
									.getQuantity());
					tax = tax + impuesto;

				} else {

					if (Catalog.getCatvalue2() == null) {
						throw new Exception(
								"No tiene cuenta asignada para impuestos");
					}
					iva_c = Catalog.getCatvalue2();
					impuesto = good.getLinetotal()
							* (Double.parseDouble(Catalog.getCatvalue()) / 100);

					tax = tax + impuesto;
				}

			}

			// sacando el total de compra

			bussines = bussines
					+ ((good.getVatsum() - impuesto) + impuesto + good
							.getLinetotal());

		}
		// consultando en la base de datos los codigos de cuenta asignados
		// cuenta de bussines partner
		buss_c = parameters.getCtlaccount();
		// buscar la cuenta asignada al almacen
		AdminDAO admin = new AdminDAO();

		BranchTO branch1 = new BranchTO();
		// buscando la cuenta asignada de cuenta de existencias al almacen

		branch1 = admin.getBranchByKey(parameters.getTowhscode());
		branch_c = branch1.getBalinvntac();
		if (branch1.getBalinvntac() == null) {
			throw new Exception(
					"No hay una cuenta contable de Inventario asignada al almacen");
		}

		// buscando cuenta asignada para IVA y FOVIAL

		// asiento contable

		JournalEntryLinesTO art1 = new JournalEntryLinesTO();
		JournalEntryLinesTO art2 = new JournalEntryLinesTO();
		JournalEntryLinesTO art3 = new JournalEntryLinesTO();
		JournalEntryLinesTO art4 = new JournalEntryLinesTO();
		JournalEntryLinesTO art5 = new JournalEntryLinesTO();
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// llenado del asiento contable
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// LLenado del padre
		List detail = new Vector();
		nuevo.setObjtype("5");
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setLoctotal(bussines);
		nuevo.setSystotal(bussines);
		nuevo.setMemo(parameters.getJrnlmemo());
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setDuedate(parameters.getDocduedate());
		nuevo.setTaxdate(parameters.getTaxdate());
		nuevo.setBtfstatus("O");
		nuevo.setTranstype(parameters.getObjtype());
		nuevo.setBaseref(parameters.getRef1());
		nuevo.setRefdate(parameters.getDocdate());
		nuevo.setRef1(parameters.getRef1());
		nuevo.setRefndrprt("N");
		nuevo.setAdjtran("N");
		nuevo.setAutostorno("N");
		nuevo.setAutovat("N");
		nuevo.setPrinted("N");
		nuevo.setAutowt("N");
		nuevo.setDeferedtax("N");

		// llenado de los
		// hijos---------------------------------------------------------------------------------------------------
		// cuenta del socio de negocio
		art1.setLine_id(1);
		art1.setCredit(bussines);
		art1.setAccount(buss_c);
		art1.setDuedate(parameters.getDocduedate());
		art1.setShortname(buss_c);
		art1.setContraact(branch_c);
		art1.setLinememo(parameters.getJrnlmemo());
		art1.setRefdate(parameters.getDocdate());
		art1.setRef1(parameters.getRef1());
		// ar1.setRef2();
		art1.setBaseref(parameters.getRef1());
		art1.setTaxdate(parameters.getTaxdate());
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
		art1.setBalduecred(bussines);
		art1.setIsnet("Y");
		art1.setTaxtype(0);
		art1.setTaxpostacc("N");
		art1.setTotalvat(0.0);
		art1.setWtliable("N");
		art1.setWtline("N");
		art1.setPayblock("N");
		art1.setOrdered("N");
		art1.setTranstype(parameters.getObjtype());
		detail.add(art1);

		// cuenta asignada al almacen
		art2.setLine_id(2);
		art2.setAccount(branch_c);
		art2.setDebit(branch);
		art2.setDuedate(parameters.getDocduedate());
		art2.setShortname(branch_c);
		art2.setContraact(buss_c);
		art2.setLinememo(parameters.getJrnlmemo());
		art2.setRefdate(parameters.getDocdate());
		art2.setRef1(parameters.getRef1());
		// art2.setRef2();
		art2.setBaseref(parameters.getRef1());
		art2.setTaxdate(parameters.getTaxdate());
		// art1.setFinncpriod(finncpriod);
		art2.setReltransid(-1);
		art2.setRellineid(-1);
		art2.setReltype("N");
		art2.setObjtype("5");
		art2.setVatline("N");
		art2.setVatamount(0.0);
		art2.setClosed("N");
		art2.setGrossvalue(0.0);
		art2.setBalduedeb(branch);
		art2.setBalduecred(0.0);
		art2.setIsnet("Y");
		art2.setTaxtype(0);
		art2.setTaxpostacc("N");
		art2.setTotalvat(0.0);
		art2.setWtliable("N");
		art2.setWtline("N");
		art2.setPayblock("N");
		art2.setOrdered("N");
		art2.setTranstype(parameters.getObjtype());
		detail.add(art2);

		// cuenta de iva
		if (tax != 0.00) {
			art3.setLine_id(3);
			art3.setDebit(tax);
			art3.setAccount(iva_c);
			art3.setDuedate(parameters.getDocduedate());
			art3.setShortname(iva_c);
			art3.setContraact(buss_c);
			art3.setLinememo(parameters.getJrnlmemo());
			art3.setRefdate(parameters.getDocdate());
			art3.setRef1(parameters.getRef1());
			// art2.setRef2();
			art3.setBaseref(parameters.getRef1());
			art3.setTaxdate(parameters.getTaxdate());
			// 3art1.setFinncpriod(finncpriod);
			art3.setReltransid(-1);
			art3.setRellineid(-1);
			art3.setReltype("N");
			art3.setObjtype("5");
			art3.setVatline("N");
			art3.setVatamount(0.0);
			art3.setClosed("N");
			art3.setGrossvalue(0.0);
			art3.setBalduedeb(tax);
			art3.setBalduecred(0.0);
			art3.setIsnet("Y");
			art3.setTaxtype(0);
			art3.setTaxpostacc("N");
			art3.setTotalvat(0.0);
			art3.setWtliable("N");
			art3.setWtline("N");
			art3.setPayblock("N");
			art3.setOrdered("N");
			art3.setTranstype(parameters.getObjtype());
			detail.add(art3);

		}

		// cuenta de cotrans y fovial si se aplica el impuesto
		if (fovc != 0.0) {
			art4.setLine_id(4);

			// art4.setCredit(fovc);
			art4.setDebit(fovc);
			art4.setAccount(fovial);
			art4.setDuedate(parameters.getDocduedate());
			art4.setShortname(fovial);
			art4.setContraact(buss_c);
			art4.setLinememo(parameters.getJrnlmemo());
			art4.setRefdate(parameters.getDocdate());
			art4.setRef1(parameters.getRef1());
			// art2.setRef2();
			art4.setBaseref(parameters.getRef1());
			art4.setTaxdate(parameters.getTaxdate());
			// rt1.setFinncpriod(finncpriod);
			art4.setReltransid(-1);
			art4.setRellineid(-1);
			art4.setReltype("N");
			art4.setObjtype("5");
			art4.setVatline("N");
			art4.setVatamount(0.0);
			art4.setClosed("N");
			art4.setGrossvalue(0.0);
			art4.setBalduedeb(fovc);
			art4.setBalduecred(0.0);
			art4.setIsnet("Y");
			art4.setTaxtype(0);
			art4.setTaxpostacc("N");
			art4.setTotalvat(0.0);
			art4.setWtliable("N");
			art4.setWtline("N");
			art4.setPayblock("N");
			art4.setOrdered("N");
			art4.setTranstype(parameters.getObjtype());
			detail.add(art4);
		}
		if (cotrans != 0.0) {
			art5.setLine_id(5);

			// art5.setCredit(cotrans);
			art5.setDebit(cotrans);
			art5.setAccount(cotrans_C);
			art5.setDuedate(parameters.getDocduedate());
			art5.setShortname(cotrans_C);
			art5.setContraact(buss_c);
			art5.setLinememo(parameters.getJrnlmemo());
			art5.setRefdate(parameters.getDocdate());
			art5.setRef1(parameters.getRef1());
			// 5rt2.setRef2();
			art5.setBaseref(parameters.getRef1());
			art5.setTaxdate(parameters.getTaxdate());
			// 54rt1.setFinncpriod(finncpriod);
			art5.setReltransid(-1);
			art5.setRellineid(-1);
			art5.setReltype("N");
			art5.setObjtype("5");
			art5.setVatline("N");
			art5.setVatamount(0.0);
			art5.setClosed("N");
			art5.setGrossvalue(0.0);
			art5.setBalduedeb(cotrans);
			art5.setBalduecred(0.0);
			art5.setIsnet("Y");
			art5.setTaxtype(0);
			art5.setTaxpostacc("N");
			art5.setTotalvat(0.0);
			art5.setWtliable("N");
			art5.setWtline("N");
			art5.setPayblock("N");
			art5.setOrdered("N");
			art5.setTranstype(parameters.getObjtype());
			detail.add(art5);
		}
		nuevo.setJournalentryList(detail);

		nuevo = fill_JournalEntry_Unir(nuevo);
		return nuevo;
	}

	public JournalEntryTO fill_JournalEntry_Unir(JournalEntryTO parameters)
			throws Exception {
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
			Double saldo = sum_debe - sum_credit;
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
			} else {
				art1.setDebit(zero);
				art1.setBalduedeb(saldo);
				art1.setBalduecred(zero);
			}

			// --------------------------------------------------------------------------------------------------------------------------------------------------------
			// llenado del asiento contable
			// --------------------------------------------------------------------------------------------------------------------------------------------------------

			art1.setLine_id(n);
			art1.setAccount(acc);
			art1.setDuedate(parameters.getDuedate());
			art1.setShortname(acc);
			art1.setContraact(c_acc);
			art1.setLinememo(parameters.getMemo());
			art1.setRefdate(parameters.getRefdate());
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

	public JournalEntryTO fill_JournalEntry_pago(PurchaseTO parameters)
			throws Exception {

		String buss_c;
		String branch_c;
		String iva_c;
		String V_local;
		String costo_venta;
		String fovialCotrans_c = null;
		double bussines = 0.0;
		double branch = 0.0;
		double tax = 0.0;
		double fovc = 0.0;
		double sale = 0.0;
		double costo = 0.0;
		double impuesto = 0.0;

		JournalEntryTO nuevo = new JournalEntryTO();
		ResultOutTO _result = new ResultOutTO();
		ArticlesTO arti = new ArticlesTO();
		boolean ind = false;
		Double total = zero;
		List list = parameters.getpurchaseDetails();
		List aux = new Vector();
		List<List> listas = new Vector();
		List aux1 = new Vector();
		// recorre la lista de detalles
		AdminDAO admin = new AdminDAO();
		CatalogTO Catalog = new CatalogTO();
		Catalog = admin.findCatalogByKey("IVA", 12);

		ParameterDAO admin1 = new ParameterDAO();
		parameterTO Catalog1 = new parameterTO();
		Catalog1 = admin1.getParameterbykey(7);

		for (Object obj : list) {
			PurchaseDetailTO good = (PurchaseDetailTO) obj;
			String cod = good.getAcctcode();
			List lisHija = new Vector();
			// calculando los impuestos y saldo de las cuentas
			// --------------------------------------------------------------------------------

			arti = good.getArticle();
			branch = branch + (arti.getAvgPrice() * good.getQuantity());
			sale = sale + good.getLinetotal();
			// calculando el iva validando si el producto esta exento o de iva
			if (good.getTaxstatus().equals("Y")) {

				impuesto = good.getLinetotal()
						* (Double.parseDouble(Catalog.getCatvalue()) / 100);
				fovc = fovc + (good.getVatsum() - impuesto);
				tax = tax + impuesto;

			}
			bussines = bussines
					+ ((good.getVatsum() - impuesto) + impuesto + good
							.getLinetotal());
		}
		// consultando en la base de datos los codigos de cuenta asignados
		// cuenta de bussines partner
		buss_c = parameters.getCtlaccount();

		// asiento contable
		JournalEntryLinesTO art1 = new JournalEntryLinesTO();
		JournalEntryLinesTO art2 = new JournalEntryLinesTO();
		JournalEntryLinesTO art3 = new JournalEntryLinesTO();
		JournalEntryLinesTO art4 = new JournalEntryLinesTO();
		JournalEntryLinesTO art5 = new JournalEntryLinesTO();
		JournalEntryLinesTO art6 = new JournalEntryLinesTO();
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// llenado del asiento contable
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// LLenado del padre
		List detail = new Vector();
		nuevo.setObjtype("5");
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setLoctotal(bussines);
		nuevo.setSystotal(bussines);
		nuevo.setMemo("Pago de factura");
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setDuedate(parameters.getDocdate());
		nuevo.setTaxdate(parameters.getTaxdate());
		nuevo.setBtfstatus("O");
		nuevo.setTranstype(parameters.getObjtype());
		nuevo.setBaseref(parameters.getRef1());
		nuevo.setRefdate(parameters.getDocduedate());
		nuevo.setRef1(parameters.getRef1());
		nuevo.setRefndrprt("N");
		nuevo.setAdjtran("N");
		nuevo.setAutostorno("N");
		nuevo.setAutovat("N");
		nuevo.setPrinted("N");
		nuevo.setAutowt("N");
		nuevo.setDeferedtax("N");

		// llenado de los
		// hijos---------------------------------------------------------------------------------------------------
		// cuenta del socio de negocio
		art1.setLine_id(1);
		// art1.setCredit(bussines);
		art1.setDebit(bussines);
		art1.setAccount(buss_c);
		art1.setDuedate(parameters.getDocduedate());
		art1.setShortname(buss_c);
		art1.setContraact(Catalog1.getValue1());
		art1.setLinememo("Pago de Factura");
		art1.setRefdate(parameters.getDocdate());
		art1.setRef1(parameters.getRef1());
		// ar1.setRef2();
		art1.setBaseref(parameters.getRef1());
		art1.setTaxdate(parameters.getTaxdate());
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
		art1.setBalduecred(bussines);
		art1.setIsnet("Y");
		art1.setTaxtype(0);
		art1.setTaxpostacc("N");
		art1.setTotalvat(0.0);
		art1.setWtliable("N");
		art1.setWtline("N");
		art1.setPayblock("N");
		art1.setOrdered("N");
		art1.setTranstype(parameters.getObjtype());
		detail.add(art1);
		// ---------------------------------------------------------------------------------------------------
		// cuenta de pago
		// ---------------------------------------------------------------------------------------------------
		art2.setLine_id(2);
		// art2.setDebit(bussines);
		art2.setCredit(bussines);
		art2.setAccount(Catalog1.getValue1());
		art2.setDuedate(parameters.getDocduedate());
		art2.setShortname(Catalog1.getValue1());
		art2.setContraact(buss_c);
		art2.setLinememo("Pago de Factura");
		art2.setRefdate(parameters.getDocdate());
		art2.setRef1(parameters.getRef1());
		// r1.setRef2();
		art2.setBaseref(parameters.getRef1());
		art2.setTaxdate(parameters.getTaxdate());
		// art1.setFinncpriod(finncpriod);
		art2.setReltransid(-1);
		art2.setRellineid(-1);
		art2.setReltype("N");
		art2.setObjtype("5");
		art2.setVatline("N");
		art2.setVatamount(0.0);
		art2.setClosed("N");
		art2.setGrossvalue(0.0);
		art2.setBalduedeb(bussines);
		art2.setBalduecred(0.0);
		art2.setIsnet("Y");
		art2.setTaxtype(0);
		art2.setTaxpostacc("N");
		art2.setTotalvat(0.0);
		art2.setWtliable("N");
		art2.setWtline("N");
		art2.setPayblock("N");
		art2.setOrdered("N");
		art2.setTranstype(parameters.getObjtype());
		detail.add(art2);

		nuevo.setJournalentryList(detail);
		return nuevo;
	}

	public ResultOutTO pagoCompras(PurchaseTO purchase, Connection conn)
			throws Exception {
		JournalEntryTO journal = new JournalEntryTO();
		ResultOutTO _return = new ResultOutTO();
		Double Monto = 0.0;
		String account;
		// parametros que se necesitan para los calculos
		Monto = purchase.getDoctotal();
		account = purchase.getCardcode();

		PurchaseInTO nuevo = new PurchaseInTO();
		nuevo.setCardcode(account);
		List lis_purchase = new Vector();
		List Aux = new Vector();
		try {
			lis_purchase = getPurchase(nuevo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Object object : Aux) {
			PurchaseTO compra = (PurchaseTO) object;
			if (!compra.getDocstatus().equals("C")) {
				Aux.add(compra);
			}

		}

		// para actualizar el documento de compras
		for (Object object : Aux) {
			PurchaseTO compra = (PurchaseTO) object;

			// si monto es diferente de cero y no esta pagada por completo
			if (Monto != zero && compra.getPaidsum() != zero
					&& compra.getPaidsum() != compra.getDoctotal()) {
				// si el monto de pago no excede la cantidad
				if ((compra.getDoctotal() - compra.getPaidsum()) <= Monto) {
					// disminuyendo la cantidad de dinero pagada
					Monto = Monto
							- (compra.getDoctotal() - compra.getPaidsum());
					compra.setPaidsum(compra.getDoctotal());
					compra.setDocstatus("C");
					compra.setPaidtodate(purchase.getCreatedate());
				} else {
					double pago = compra.getPaidsum() + Monto;
					compra.setPaidsum(pago);
					compra.setPaidtodate(purchase.getCreatedate());
					Monto = 0.0;

				}
			}

			// si monto es diferente de cero y no ha se ha pagado ni una parte
			// de la factura
			if (Monto != zero && compra.getPaidsum() == zero) {
				if (compra.getDoctotal() <= Monto) {
					compra.setPaidsum(compra.getDoctotal());
					Monto = Monto - compra.getDoctotal();
					compra.setDocstatus("C");
					compra.setPaidtodate(purchase.getCreatedate());
				} else {
					double pago = Monto;
					compra.setPaidsum(pago);
					compra.setPaidtodate(purchase.getCreatedate());
					Monto = 0.0;
				}
			}

		}

		for (Object object : Aux) {
			PurchaseTO compra = (PurchaseTO) object;

			// actualizacion de el total de facturas
			_return = inv_Purchase_update(compra, Common.MTTOUPDATE, conn);

		}
		return _return;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Consultas compras
	// -----------------------------------------------------------------------------------------------------------------
	public List getPurchase(PurchaseInTO param) throws Exception {
		// TODO Auto-generated method stub
		List _return;
		PurchaseDAO DAO = new PurchaseDAO();

		try {
			_return = DAO.getPurchase(param);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public PurchaseTO getPurchaseByKey(int docentry) throws Exception {
		// TODO Auto-generated method stub
		PurchaseTO _return = new PurchaseTO();
		PurchaseDAO DAO = new PurchaseDAO();
		try {
			_return = DAO.getPurchaseByKey(docentry);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}

		return _return;
	}

	public List getPurchaseDetail(int docentry) throws Exception {
		// TODO Auto-generated method stub
		List _return;
		PurchaseDetailDAO DAO = new PurchaseDetailDAO();

		try {
			_return = DAO.getpurchaseDetail(docentry);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// MAntenimeinto de Ordenes de compras
	// -----------------------------------------------------------------------------------------------------------------

	public List getPurchaseQuotation(PurchaseQuotationInTO param)
			throws Exception {
		// TODO Auto-generated method stub
		List _return;
		PurchaseQuotationDAO DAO = new PurchaseQuotationDAO();

		try {
			_return = DAO.getPurchaseQuotation(param);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public PurchaseQuotationTO getPurchaseQuotationByKey(int docentry)
			throws Exception {
		// TODO Auto-generated method stub
		PurchaseQuotationTO _return = new PurchaseQuotationTO();
		PurchaseQuotationDAO DAO = new PurchaseQuotationDAO();
		try {
			_return = DAO.getPurchaseQuotationByKey(docentry);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}

		return _return;
	}

	public ResultOutTO inv_PurchaseQuotation_mtto(
			PurchaseQuotationTO parameters, int action) throws Exception {
		// TODO Auto-generated method stub
		// pasar el codigo de almacen a los hijos
		Iterator<PurchaseQuotationDetailTO> iterator3 = parameters
				.getPurchaseQuotationDetails().iterator();
		while (iterator3.hasNext()) {

			PurchaseQuotationDetailTO articleDetalle = (PurchaseQuotationDetailTO) iterator3
					.next();
			articleDetalle.setWhscode(parameters.getTowhscode());
		}
		// -------------------------------------------------------------------------------------------------------------------------------------
		// validaciones-------------------------------------------------------------------------------------------------------------------------

		ResultOutTO _return = new ResultOutTO();
		_return = validate_inv_PurchaseQuotation_mtto(parameters);

		if (_return.getCodigoError() != 0) {
			return _return;
		}
		Double total = 0.0;
		PurchaseQuotationDAO DAO = new PurchaseQuotationDAO();
		DAO.setIstransaccional(true);
		PurchaseQuotationDetailDAO goodDAO1 = new PurchaseQuotationDetailDAO(
				DAO.getConn());
		goodDAO1.setIstransaccional(true);
		try {
			Iterator<PurchaseQuotationDetailTO> iterator2 = parameters
					.getPurchaseQuotationDetails().iterator();
			while (iterator2.hasNext()) {
				PurchaseQuotationDetailTO articleDetalle = (PurchaseQuotationDetailTO) iterator2
						.next();
				articleDetalle.setDiscprcnt(articleDetalle.getQuantity());
				articleDetalle.setOpenqty(articleDetalle.getQuantity());
				articleDetalle.setFactor1(articleDetalle.getQuantity());
				articleDetalle.setObjtype("22");

			}

			parameters.setDiscsum(0.00);
			parameters.setNret(0.00);
			parameters.setPaidsum(0.00);
			parameters.setRounddif(0.00);
			parameters.setVatsum(0.00);
			parameters.setObjtype("22");
			_return.setDocentry(DAO.inv_PurchaseQuotation_mtto(parameters,
					action));

			Iterator<PurchaseQuotationDetailTO> iterator = parameters
					.getPurchaseQuotationDetails().iterator();
			while (iterator.hasNext()) {
				PurchaseQuotationDetailTO articleDetalle = (PurchaseQuotationDetailTO) iterator
						.next();
				// Para articulos nuevos
				articleDetalle.setDocentry(_return.getDocentry());
				if (action == Common.MTTOINSERT) {
					goodDAO1.inv_PurchaseQuotationDetail_mtto(articleDetalle,
							Common.MTTOINSERT);
				}
				if (action == Common.MTTODELETE) {
					goodDAO1.inv_PurchaseQuotationDetail_mtto(articleDetalle,
							Common.MTTODELETE);
				}
			}
			DAO.forceCommit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			DAO.rollBackConnection();
			throw (EJBException) new EJBException(e);
		} finally {

			DAO.forceCloseConnection();
		}
		_return.setCodigoError(0);
		_return.setMensaje("Datos guardados correctamente");
		return _return;
	}

	public ResultOutTO validate_inv_PurchaseQuotation_mtto(
			PurchaseQuotationTO parameters) throws Exception {

		boolean valid = false;
		ResultOutTO _return = new ResultOutTO();
		AccountingEJB acc = new AccountingEJB();
		CatalogEJB Businesspartner = new CatalogEJB();
		AdminEJB EJB1 = new AdminEJB();
		List branch = new Vector();
		ArticlesTO DBArticle = new ArticlesTO();
		String code;
		// ------------------------------------------------------------------------------------------------------------
		// validaciones
		// ------------------------------------------------------------------------------------------------------------

		// ------------------------------------------------------------------------------------------------------------
		// Validación almacen bloqueado
		// ------------------------------------------------------------------------------------------------------------
		if (parameters.getTowhscode() == null) {
			_return.setCodigoError(1);
			_return.setMensaje("Codigo de almacen null");

			return _return;
		}
		_return = EJB1.validate_branchActiv(parameters.getTowhscode());

		if (_return.getCodigoError() != 0) {
			_return.setCodigoError(1);
			_return.setMensaje("El Almacen no esta activo");

			return _return;
		}
		// ------------------------------------------------------------------------------------------------------------
		// Validación de fecha de periodo contable
		// ------------------------------------------------------------------------------------------------------------
		if (parameters.getDocdate() == null) {
			_return.setCodigoError(1);
			_return.setMensaje("no se encuentra fecha");

			return _return;
		}
		_return = acc.validate_exist_accperiod(parameters.getDocdate());
		if (_return.getCodigoError() != 0) {
			_return.setCodigoError(1);
			_return.setMensaje("El documento tiene una fecha Fuera del periodo contable activo");
			return _return;
		}
		// ------------------------------------------------------------------------------------------------------------
		// Validación del socio de negocio
		// ------------------------------------------------------------------------------------------------------------
		if (parameters.getCardcode() == null) {
			_return.setCodigoError(1);
			_return.setMensaje("Codigo de socio vacio");

			return _return;
		}
		_return = Businesspartner.validate_businesspartnerBykey(parameters
				.getCardcode());
		if (_return.getCodigoError() != 0) {
			_return.setCodigoError(1);
			_return.setMensaje("el socio de negocio no esta activo para esta transaccion");
			return _return;
		}

		Iterator<PurchaseQuotationDetailTO> iterator1 = parameters
				.getPurchaseQuotationDetails().iterator();

		// recorre el ClientCrediDetail
		while (iterator1.hasNext()) {
			AdminEJB EJB = new AdminEJB();
			// Consultar información actualizada desde la base
			PurchaseQuotationDetailTO PurchaseQuotationDetail = (PurchaseQuotationDetailTO) iterator1
					.next();
			code = PurchaseQuotationDetail.getItemcode();

			DBArticle = EJB.getArticlesByKey(code);

			// ------------------------------------------------------------------------------------------------------------
			// Validación articulo existe
			// ------------------------------------------------------------------------------------------------------------
			valid = false;
			if (DBArticle != null) {
				valid = true;
			}

			if (!valid) {
				_return.setLinenum(PurchaseQuotationDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ PurchaseQuotationDetail.getItemcode() + " "
						+ PurchaseQuotationDetail.getDscription()

						+ " no existe,informar al administrador. linea :"
						+ PurchaseQuotationDetail.getLinenum());

				return _return;

			}

			// ------------------------------------------------------------------------------------------------------------
			// Validación articulo activo
			// ------------------------------------------------------------------------------------------------------------

			valid = false;
			if (DBArticle.getValidFor() != null
					&& DBArticle.getValidFor().toUpperCase().equals("Y")) {
				valid = true;
			}

			if (!valid) {
				_return.setLinenum(PurchaseQuotationDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ PurchaseQuotationDetail.getItemcode() + " "
						+ PurchaseQuotationDetail.getDscription()

						+ " No esta activo. linea :"
						+ PurchaseQuotationDetail.getLinenum());

				return _return;

			}

			// ------------------------------------------------------------------------------------------------------------
			// Validación articulo de compra
			// ------------------------------------------------------------------------------------------------------------
			valid = false;
			if (DBArticle.getPrchseItem() != null
					&& DBArticle.getPrchseItem().toUpperCase().equals("Y")) {
				valid = true;
			}

			if (!valid) {
				_return.setLinenum(PurchaseQuotationDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ PurchaseQuotationDetail.getItemcode() + " "
						+ PurchaseQuotationDetail.getDscription()
						+ " No es un articulo de venta. linea :"
						+ PurchaseQuotationDetail.getLinenum());
				return _return;
			}

			// ------------------------------------------------------------------------------------------------------------
			// Validación almacen bloqueado para articulo
			// ------------------------------------------------------------------------------------------------------------
			valid = false;

			branch = DBArticle.getBranchArticles();

			for (Object object : branch) {
				BranchArticlesTO branch1 = (BranchArticlesTO) object;

				if (branch1.getWhscode().equals(
						PurchaseQuotationDetail.getWhscode())) {
					if (branch1.isIsasociated() && branch1.getLocked() != null
							&& branch1.getLocked().toUpperCase().equals("F")) {
						valid = true;
					}
				}
			}

			if (!valid) {
				_return.setLinenum(PurchaseQuotationDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ PurchaseQuotationDetail.getItemcode()
						+ " "
						+ PurchaseQuotationDetail.getDscription()
						+ " No esta asignado o esta bloquedo para el almacen indicado. linea :"
						+ PurchaseQuotationDetail.getLinenum());
				return _return;
			}

		}
		_return.setCodigoError(0);

		return _return;

	}

	// -----------------------------------------------------------------------------------------------------------------
	// Mantenimeinto de Notas de Credito Compras
	// -----------------------------------------------------------------------------------------------------------------
	public ResultOutTO inv_Supplier_mtto(SupplierTO parameters, int action)
			throws EJBException {

		// Declaración de variables

		ResultOutTO _valid = new ResultOutTO();
		ResultOutTO _return = new ResultOutTO();
		SupplierDAO DAO = new SupplierDAO();

		// --------------------------------------------------------------------------------------------------------------------------------
		// Validar acción a realizar
		// --------------------------------------------------------------------------------------------------------------------------------

		if (action != Common.MTTOINSERT) {
			_return = inv_Supplier_update(parameters, action);
			return _return;
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Asignación de valores por defecto y llenado:
		// Estas se realizan solo para cuando es guardar, el actualizar y borrar
		// no aplican.
		// --------------------------------------------------------------------------------------------------------------------------------
		parameters = fillSupplier(parameters);

		// --------------------------------------------------------------------------------------------------------------------------------
		// Hacer validaciones:
		// Estas se realizan solo para cuando es guardar, el actualizar y borrar
		// no aplican para validaciones
		// --------------------------------------------------------------------------------------------------------------------------------

		_valid = validate_inv_supplier_mtto(parameters);

		if (_valid.getCodigoError() != 0) {
			return _valid;
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Guardar en base:
		// Desde aqui se debe manajar como una transaccion global, cada metodo
		// debe tener una opción para ejecutarlo como parte de una transacción
		// global
		// --------------------------------------------------------------------------------------------------------------------------------

		try {
			DAO.setIstransaccional(true);
			_return = save_TransactionSupplier(parameters, action,
					DAO.getConn());
			DAO.forceCommit();

		} catch (Exception e) {
			DAO.rollBackConnection();
			throw (EJBException) new EJBException(e);
		} finally {

			DAO.forceCloseConnection();
		}

		_return.setCodigoError(0);
		_return.setMensaje("Datos guardados con exito");
		return _return;
	}

	public ResultOutTO inv_Supplier_update(SupplierTO parameters, int action)
			throws EJBException {
		ResultOutTO _return = new ResultOutTO();
		PurchaseDAO DAO = new PurchaseDAO();
		try {
			_return = inv_Supplier_update(parameters, action, DAO.getConn());
			DAO.forceCommit();
		} catch (Exception e) {
			DAO.rollBackConnection();
			throw (EJBException) new EJBException(e);
		} finally {
			DAO.forceCloseConnection();
		}
		_return.setCodigoError(0);
		_return.setMensaje("Datos Almacenados con Exito");
		return _return;

	}

	public ResultOutTO inv_Supplier_update(SupplierTO parameters, int action,
			Connection conn) throws Exception {
		// Variables
		ResultOutTO _return = new ResultOutTO();
		SupplierDAO DAO = new SupplierDAO(conn);
		DAO.setIstransaccional(true);
		SupplierDetailDAO DAO1 = new SupplierDetailDAO(conn);
		DAO1.setIstransaccional(true);

		// Actualizar/borrar encabezados
		_return.setDocentry(DAO.inv_Supplier_mtto(parameters, action));

		// Borrar detalles
		Iterator<SupplierDetailTO> iterator = parameters.getsupplierDetails()
				.iterator();
		if (action == Common.MTTODELETE) {
			while (iterator.hasNext()) {
				SupplierDetailTO detalleReceipt = (SupplierDetailTO) iterator
						.next();

				DAO1.inv_SupplierDetail_mtto(detalleReceipt, Common.MTTODELETE);
			}
		}

		_return.setCodigoError(0);
		_return.setMensaje("Datos Actualizados con exito");
		return _return;
	}

	private SupplierTO fillSupplier(SupplierTO parameters) {

		// variables
		Double total = zero;
		Double vatsum = zero;
		int baseentry = 0;
		ArticlesTO DBArticle = new ArticlesTO();
		AdminDAO admin = new AdminDAO();

		BranchTO branch1 = new BranchTO();
		// buscando la cuenta asignada de cuenta de existencias al almacen

		try {
			branch1 = admin.getBranchByKey(parameters.getTowhscode());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String branch_c = branch1.getBalinvntac();
		// --------------------------------------------------------------------------------------------------------------------------------
		// Valores por defecto detalle
		// --------------------------------------------------------------------------------------------------------------------------------
		@SuppressWarnings("unchecked")
		Iterator<SupplierDetailTO> iterator = parameters.getsupplierDetails()
				.iterator();

		while (iterator.hasNext()) {
			SupplierDetailTO articleDetalle = (SupplierDetailTO) iterator
					.next();

			AdminEJB EJB = new AdminEJB();
			DBArticle = EJB.getArticlesByKey(articleDetalle.getItemcode());

			// Asignar a documento
			articleDetalle.setArticle(DBArticle);
			articleDetalle.setAcctcode(branch_c);
			// Valores por defecto
			articleDetalle.setDocentry(parameters.getDocentry());
			articleDetalle.setTargettype(-1);
			// articleDetalle.setBaseref("");
			articleDetalle.setBasetype(-1);
			articleDetalle.setLinestatus("O");
			articleDetalle.setDscription(DBArticle.getItemName());
			articleDetalle.setDiscprcnt(zero);
			articleDetalle.setWhscode(parameters.getTowhscode());
			// articleDetalle.setAcctcode(acctcode);
			articleDetalle.setTaxstatus("Y");
			articleDetalle.setOcrcode(parameters.getTowhscode());
			articleDetalle.setFactor1(zero);
			articleDetalle.setObjtype("21");
			articleDetalle.setGrssprofit(articleDetalle.getGrssprofit());
			articleDetalle.setVatappld(articleDetalle.getVatappld());
			articleDetalle.setUnitmsr(DBArticle.getBuyUnitMsr());
			articleDetalle.setStockpricestockprice(articleDetalle
					.getStockpricestockprice());

			// Calculo de impuesto
			vatsum = vatsum + articleDetalle.getVatsum();

			// Calculo de totales
			articleDetalle.setLinetotal(articleDetalle.getQuantity()
					* articleDetalle.getPrice());
			articleDetalle.setOpenqty(articleDetalle.getQuantity());
			total = total + articleDetalle.getGtotal();
			baseentry = articleDetalle.getBaseentry();

		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Valores por defecto encabezado
		// --------------------------------------------------------------------------------------------------------------------------------
		parameters.setDocnum(parameters.getDocentry());
		parameters.setDoctype("I");
		parameters.setCanceled("N");
		parameters.setDocstatus("O");
		parameters.setObjtype("21");
		parameters.setVatsum(vatsum);
		parameters.setDiscsum(zero);
		parameters.setDoctotal(total);
		parameters.setRef1(Integer.toString(parameters.getDocnum()));
		parameters.setJrnlmemo("Nota de credito proveedores - "
				+ parameters.getCardcode());
		// parameters.setReceiptnum(0);
		parameters.setGroupnum(0);
		parameters.setConfirmed("Y");
		parameters.setCreatetran("Y");
		// parameters.setSeries(0);
		parameters.setRounddif(zero);
		parameters.setRounding("N");
		// parameters.setCtlaccount(ctlaccount); Aqui deberia de hacerse la
		// consulta he incluirse la cuenta contables
		parameters.setPaidsum(zero);
		parameters.setNret(zero);
		parameters.setReceiptnum(baseentry);
		return parameters;
	}

	public ResultOutTO save_TransactionSupplier(SupplierTO purchase,
			int action, Connection conn) throws Exception {

		// Variables
		List transactions = new Vector();
		InventoryLogTO inventoryLog = new InventoryLogTO();
		WarehouseJournalTO warehouseJournal = new WarehouseJournalTO();
		WarehouseJournalLayerTO warehouseJournallayer = new WarehouseJournalLayerTO();
		ResultOutTO _return = new ResultOutTO();
		ResultOutTO res_invet = new ResultOutTO();
		ResultOutTO res_whj = new ResultOutTO();
		ResultOutTO res_whjl = new ResultOutTO();
		ResultOutTO res_jour = new ResultOutTO();
		ResultOutTO res_UpdateOnhand = new ResultOutTO();
		SupplierDAO DAO = new SupplierDAO(conn);
		transactionEJB trans = new transactionEJB();
		JournalEntryTO journal = new JournalEntryTO();

		// --------------------------------------------------------------------------------------------------------------------------------
		// Guardar Encabezados y detalles de Entrada
		// --------------------------------------------------------------------------------------------------------------------------------

		_return = inv_Supplier_save(purchase, action, conn);
		purchase.setDocentry(_return.getDocentry());
		purchase.setDocnum(_return.getDocentry());
		purchase.setRef1(Integer.toString(purchase.getDocnum()));

		// --------------------------------------------------------------------------------------------------------------------------------
		// Llenar objeto tipo transacción
		// --------------------------------------------------------------------------------------------------------------------------------

		transactions = fill_transaction(purchase);

		// --------------------------------------------------------------------------------------------------------------------------------
		// Calculo de existencias y costos
		// --------------------------------------------------------------------------------------------------------------------------------
		for (Object object : transactions) {
			TransactionTo ivt = (TransactionTo) object;
			ivt = trans.calculate(ivt);
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Registro de Inventory Log
		// --------------------------------------------------------------------------------------------------------------------------------

		for (Object object : transactions) {
			TransactionTo ivt = (TransactionTo) object;

			inventoryLog = trans.fill_Inventory_Log(ivt);
			res_invet = trans.save_Inventory_Log(inventoryLog, conn);
			// Asignación de codigo MessageId
			ivt.setMessageid(res_invet.getDocentry());
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Registro de Warehouse Journal
		// --------------------------------------------------------------------------------------------------------------------------------

		for (Object object : transactions) {
			TransactionTo ivt = (TransactionTo) object;
			warehouseJournal = trans.fill_WarehouseJournal(ivt);
			// Asiganción de TransSeq
			res_whj = trans.save_WarehouseJournal(warehouseJournal, conn);
			ivt.setTransseq(res_whj.getDocentry());
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Registro de Warehouse Journal layer
		// --------------------------------------------------------------------------------------------------------------------------------

		for (Object object : transactions) {

			TransactionTo ivt = (TransactionTo) object;
			warehouseJournallayer = trans.fill_WarehouseJournalLayer(ivt);
			// Asiganción de TransSeq
			res_whjl = trans.save_WarehouseJournalLayer(warehouseJournallayer,
					conn);
		}

		// --------------------------------------------------------------------------------------------------------------------------------
		// Actualizacion de existencia articulos y almacenes
		// --------------------------------------------------------------------------------------------------------------------------------

		for (Object object : transactions) {
			TransactionDAO transDAO = new TransactionDAO(conn);
			transDAO.setIstransaccional(true);
			TransactionTo ivt = (TransactionTo) object;
			res_UpdateOnhand = transDAO.Update_Onhand_articles(ivt);
		}

		PurchaseTO purch = new PurchaseTO();
		purch = getPurchaseByKey(purchase.getReceiptnum());

		if (!purch.equals(null)) {

			if (!purch.getDocstatus().equals("C")) {
				// -----------------------------------------------------------------------------------
				// registro del asiento contable y actualización de saldos
				// -----------------------------------------------------------------------------------

				journal = fill_JournalEntry(purchase);

				AccountingEJB account = new AccountingEJB();
				res_jour = account.journalEntry_mtto(journal,
						Common.MTTOINSERT, conn);

				// -----------------------------------------------------------------------------------
				// Actualización de documento con datos de Asiento contable
				// -----------------------------------------------------------------------------------

				purchase.setTransid(res_jour.getDocentry());
				_return = inv_Supplier_update(purchase, Common.MTTOUPDATE, conn);

			} else {

				// -----------------------------------------------------------------------------------
				// registro del asiento contable cuando ya a sido pagado y
				// actualización de saldos
				// -----------------------------------------------------------------------------------

				journal = fill_JournalEntry_pago(purchase);

				AccountingEJB account1 = new AccountingEJB();
				res_jour = account1.journalEntry_mtto(journal,
						Common.MTTOINSERT, conn);

				// -----------------------------------------------------------------------------------
				// Actualización de documento con datos de Asiento contable
				// -----------------------------------------------------------------------------------

				purchase.setPaidtodate(purchase.getDocdate());
				purchase.setPaidsum(journal.getSystotal());
				// -----------------------------------------------------------------------------------
				// registro del asiento contable y actualización de saldos
				// -----------------------------------------------------------------------------------

				journal = fill_JournalEntry(purchase);

				AccountingEJB account = new AccountingEJB();
				res_jour = account.journalEntry_mtto(journal,
						Common.MTTOINSERT, conn);

				// -----------------------------------------------------------------------------------
				// Actualización de documento con datos de Asiento contable
				// -----------------------------------------------------------------------------------

				purchase.setTransid(res_jour.getDocentry());
				_return = inv_Supplier_update(purchase, Common.MTTOUPDATE, conn);
			}
			// -----------------------------------------------------------------------------------
			// Actualización de documento de ventas
			// -----------------------------------------------------------------------------------

			ResultOutTO res_sales = new ResultOutTO();
			purch = update_lines(purch, purchase);
			res_sales = inv_Purchase_update(purch, Common.MTTOUPDATE, conn);

		} else {
			// -----------------------------------------------------------------------------------
			// registro del asiento contable y actualización de saldos
			// -----------------------------------------------------------------------------------

			journal = fill_JournalEntry(purchase);

			AccountingEJB account = new AccountingEJB();
			res_jour = account.journalEntry_mtto(journal, Common.MTTOINSERT,
					conn);

			// -----------------------------------------------------------------------------------
			// Actualización de documento con datos de Asiento contable
			// -----------------------------------------------------------------------------------

			purchase.setTransid(res_jour.getDocentry());
			_return = inv_Supplier_update(purchase, Common.MTTOUPDATE, conn);

		}

		return _return;
	}

	private List fill_transaction(SupplierTO document) {
		List _return = new Vector();

		Iterator<SupplierDetailTO> iterator = document.getsupplierDetails()
				.iterator();
		while (iterator.hasNext()) {
			SupplierDetailTO detail = (SupplierDetailTO) iterator.next();

			TransactionTo transaction = new TransactionTo();
			transaction.setTransseq(0);
			transaction.setDocentry(document.getDocentry());
			transaction.setDocnum(Integer.toString(document.getDocnum()));
			transaction.setDocduedate(document.getDocduedate());
			transaction.setDocdate(document.getDocdate());
			transaction.setComment(document.getComments());
			transaction.setJrnlmemo(document.getJrnlmemo());
			transaction.setUsersign(document.getUsersign());
			transaction.setRef1(Integer.toString(document.getDocnum()));
			transaction.setRef2(document.getNumatcard());
			transaction.setLinenum(detail.getLinenum());
			transaction.setItemcode(detail.getItemcode());
			transaction.setDscription(detail.getDscription());
			transaction.setQuantity(detail.getQuantity());
			transaction.setPrice(detail.getPrice());
			transaction.setLinetotal(detail.getLinetotal());
			transaction.setWhscode(detail.getWhscode());
			transaction.setAcctcode(detail.getAcctcode());
			transaction.setOcrcode(document.getCardcode());
			transaction.setVatgroup(detail.getVatgroup());
			transaction.setPriceafvat(detail.getPriceafvat());
			transaction.setVatsum(detail.getVatsum());
			transaction.setObjtype(detail.getObjtype());
			transaction.setGrssprofit(detail.getGrssprofit());
			transaction.setTaxcode(detail.getTaxcode());
			transaction.setVatappld(detail.getVatappld());
			transaction.setStockprice(detail.getPrice());
			transaction.setGtotal(detail.getGtotal());
			transaction.setInqty(zero);
			transaction.setOutqty(zero);
			transaction.setMessageid(0);
			transaction.setBalance(zero);
			transaction.setNewOnhand(zero);
			transaction.setNewWhsOnhand(zero);
			transaction.setNewAvgprice(zero);
			transaction.setArticle(detail.getArticle());

			_return.add(transaction);
		}
		return _return;
	}

	public ResultOutTO inv_Supplier_save(SupplierTO parameters, int action,
			Connection conn) throws Exception {
		// Variables
		ResultOutTO _return = new ResultOutTO();
		SupplierDAO DAO = new SupplierDAO(conn);
		DAO.setIstransaccional(true);
		SupplierDetailDAO goodDAO1 = new SupplierDetailDAO(conn);
		goodDAO1.setIstransaccional(true);

		// --------------------------------------------------------------------------------------------------------------------------------
		// Guardar encabezados
		// --------------------------------------------------------------------------------------------------------------------------------

		_return.setDocentry(DAO.inv_Supplier_mtto(parameters, action));

		// --------------------------------------------------------------------------------------------------------------------------------
		// Guardar detalles
		// --------------------------------------------------------------------------------------------------------------------------------

		Iterator<SupplierDetailTO> iterator = parameters.getsupplierDetails()
				.iterator();
		while (iterator.hasNext()) {
			SupplierDetailTO detalleReceipt = (SupplierDetailTO) iterator
					.next();

			detalleReceipt.setDocentry(_return.getDocentry());
			goodDAO1.inv_SupplierDetail_mtto(detalleReceipt, Common.MTTOINSERT);

		}

		_return.setCodigoError(0);
		_return.setMensaje("Datos guardados con exito");
		return _return;
	}

	public JournalEntryTO fill_JournalEntry(SupplierTO parameters)
			throws Exception {

		String buss_c;
		String branch_c;
		String iva_c = null;
		String V_local;
		String costo_venta;
		String fovial = null;
		String cotrans_C = null;
		double bussines = 0.0;
		double branch = 0.0;
		double tax = 0.0;
		double fovc = 0.0;
		double cotrans = 0.0;
		double sale = 0.0;
		double costo = 0.0;
		double impuesto = 0.0;

		JournalEntryTO nuevo = new JournalEntryTO();
		ResultOutTO _result = new ResultOutTO();
		boolean ind = false;
		Double total = zero;
		List list = parameters.getsupplierDetails();
		List aux = new Vector();
		List<List> listas = new Vector();
		List aux1 = new Vector();
		// recorre la lista de detalles

		ArticlesTO arti = new ArticlesTO();

		for (Object obj : list) {
			SupplierDetailTO good = (SupplierDetailTO) obj;
			String cod = good.getAcctcode();
			List lisHija = new Vector();
			CatalogTO Catalog = new CatalogTO();
			AdminDAO admin = new AdminDAO();
			Catalog = admin.findCatalogByKey(good.getTaxcode(), 12);
			// calculando los impuestos y saldo de las cuentas
			// --------------------------------------------------------------------------------

			arti = good.getArticle();
			branch = branch + (good.getPrice() * good.getQuantity());
			sale = sale + good.getLinetotal();
			// calculando el iva validando si el producto esta exento o de iva
			if (good.getTaxstatus().equals("Y")) {
				// validar si es FOV
				if (good.getTaxcode().equals("FOV")) {

					admin = new AdminDAO();
					CatalogTO Catalog1 = new CatalogTO();
					Catalog1 = admin.findCatalogByKey("FOV1", 12);
					if (Catalog1.getCatvalue3() == null) {
						throw new Exception(
								"No tiene cuenta asignada para  impuestos");
					}
					if (Catalog1.getCatvalue2() == null) {
						throw new Exception(
								"No tiene cuenta asignada para  impuestos");
					}
					if (Catalog1.getCatvalue() == null) {
						throw new Exception(
								"No tiene cuenta asignada para  impuestos");
					}
					cotrans_C = Catalog1.getCatvalue3();
					fovial = Catalog1.getCatvalue2();
					iva_c = Catalog1.getCatvalue();

					impuesto = good.getLinetotal()
							* (Double.parseDouble(Catalog.getCatvalue()) / 100);
					fovc = fovc
							+ (Double.parseDouble(Catalog.getCatvalue2()) * good
									.getQuantity());
					cotrans = cotrans
							+ (Double.parseDouble(Catalog.getCatvalue3()) * good
									.getQuantity());
					tax = tax + impuesto;

				} else {

					if (Catalog.getCatvalue2() == null) {
						throw new Exception(
								"No tiene cuenta asignada para impuestos");
					}
					iva_c = Catalog.getCatvalue2();
					impuesto = good.getLinetotal()
							* (Double.parseDouble(Catalog.getCatvalue()) / 100);

					tax = tax + impuesto;
				}

			}

			// sacando el total de venta

			bussines = bussines
					+ ((good.getVatsum() - impuesto) + impuesto + good
							.getLinetotal());

		}
		// consultando en la base de datos los codigos de cuenta asignados
		// cuenta de bussines partner
		buss_c = parameters.getCtlaccount();
		// buscar la cuenta asignada al almacen
		AdminDAO admin = new AdminDAO();

		BranchTO branch1 = new BranchTO();
		// buscando la cuenta asignada de cuenta de existencias al almacen

		branch1 = admin.getBranchByKey(parameters.getTowhscode());
		branch_c = branch1.getBalinvntac();
		if (branch1.getBalinvntac() == null) {
			throw new Exception(
					"No hay una cuenta contable de Inventario asignada al almacen");
		}

		// buscando cuenta asignada para IVA y FOVIAL

		// asiento contable

		JournalEntryLinesTO art1 = new JournalEntryLinesTO();
		JournalEntryLinesTO art2 = new JournalEntryLinesTO();
		JournalEntryLinesTO art3 = new JournalEntryLinesTO();
		JournalEntryLinesTO art4 = new JournalEntryLinesTO();
		JournalEntryLinesTO art5 = new JournalEntryLinesTO();
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// llenado del asiento contable
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// LLenado del padre
		List detail = new Vector();
		nuevo.setObjtype("5");
		nuevo.setMemo(parameters.getJrnlmemo());
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setLoctotal(bussines);
		nuevo.setSystotal(bussines);
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setDuedate(parameters.getDocdate());
		nuevo.setTaxdate(parameters.getTaxdate());
		nuevo.setBtfstatus("O");
		nuevo.setTranstype(parameters.getObjtype());
		nuevo.setBaseref(parameters.getRef1());
		nuevo.setRefdate(parameters.getDocduedate());
		nuevo.setRef1(parameters.getRef1());
		nuevo.setRefndrprt("N");
		nuevo.setAdjtran("N");
		nuevo.setAutostorno("N");
		nuevo.setAutovat("N");
		nuevo.setPrinted("N");
		nuevo.setAutowt("N");
		nuevo.setDeferedtax("N");

		// llenado de los
		// hijos---------------------------------------------------------------------------------------------------
		// cuenta del socio de negocio
		art1.setLine_id(1);
		// art1.setCredit(bussines);
		art1.setDebit(bussines);
		art1.setAccount(buss_c);
		art1.setDuedate(parameters.getDocduedate());
		art1.setShortname(buss_c);
		art1.setContraact(branch_c);
		art1.setLinememo(parameters.getJrnlmemo());
		art1.setRefdate(parameters.getDocdate());
		art1.setRef1(parameters.getRef1());
		// ar1.setRef2();
		art1.setBaseref(parameters.getRef1());
		art1.setTaxdate(parameters.getTaxdate());
		// art1.setFinncpriod(finncpriod);
		art1.setReltransid(-1);
		art1.setRellineid(-1);
		art1.setReltype("N");
		art1.setObjtype("5");
		art1.setVatline("N");
		art1.setVatamount(0.0);
		art1.setClosed("N");
		art1.setGrossvalue(0.0);
		art1.setBalduedeb(bussines);
		art1.setBalduecred(0.0);
		art1.setIsnet("Y");
		art1.setTaxtype(0);
		art1.setTaxpostacc("N");
		art1.setTotalvat(0.0);
		art1.setWtliable("N");
		art1.setWtline("N");
		art1.setPayblock("N");
		art1.setOrdered("N");
		art1.setTranstype(parameters.getObjtype());
		detail.add(art1);

		// cuenta asignada al almacen
		art2.setLine_id(2);
		art2.setAccount(branch_c);
		// art2.setDebit(branch);
		art2.setCredit(branch);
		art2.setDuedate(parameters.getDocduedate());
		art2.setShortname(branch_c);
		art2.setContraact(buss_c);
		art2.setLinememo(parameters.getJrnlmemo());
		art2.setRefdate(parameters.getDocdate());
		art2.setRef1(parameters.getRef1());
		// art2.setRef2();
		art2.setBaseref(parameters.getRef1());
		art2.setTaxdate(parameters.getTaxdate());
		// art1.setFinncpriod(finncpriod);
		art2.setReltransid(-1);
		art2.setRellineid(-1);
		art2.setReltype("N");
		art2.setObjtype("5");
		art2.setVatline("N");
		art2.setVatamount(0.0);
		art2.setClosed("N");
		art2.setGrossvalue(0.0);
		art2.setBalduedeb(0.0);
		art2.setBalduecred(branch);
		art2.setIsnet("Y");
		art2.setTaxtype(0);
		art2.setTaxpostacc("N");
		art2.setTotalvat(0.0);
		art2.setWtliable("N");
		art2.setWtline("N");
		art2.setPayblock("N");
		art2.setOrdered("N");
		art2.setTranstype(parameters.getObjtype());
		detail.add(art2);

		if (tax != 0.00) {
			// cuenta de iva
			art3.setLine_id(3);
			art3.setCredit(tax);
			// art3.setDebit(tax);
			art3.setAccount(iva_c);
			art3.setDuedate(parameters.getDocduedate());
			art3.setShortname(iva_c);
			art3.setContraact(buss_c);
			art3.setLinememo(parameters.getJrnlmemo());
			art3.setRefdate(parameters.getDocdate());
			art3.setRef1(parameters.getRef1());
			// art2.setRef2();
			art3.setBaseref(parameters.getRef1());
			art3.setTaxdate(parameters.getTaxdate());
			// 3art1.setFinncpriod(finncpriod);
			art3.setReltransid(-1);
			art3.setRellineid(-1);
			art3.setReltype("N");
			art3.setObjtype("5");
			art3.setVatline("N");
			art3.setVatamount(0.0);
			art3.setClosed("N");
			art3.setGrossvalue(0.0);
			art3.setBalduedeb(0.0);
			art3.setBalduecred(tax);
			art3.setIsnet("Y");
			art3.setTaxtype(0);
			art3.setTaxpostacc("N");
			art3.setTotalvat(0.0);
			art3.setWtliable("N");
			art3.setWtline("N");
			art3.setPayblock("N");
			art3.setOrdered("N");
			art3.setTranstype(parameters.getObjtype());
			detail.add(art3);
		}
		// cuenta de cotrans y fovial si se aplica el impuesto
		if (fovc != 0.0) {
			art4.setLine_id(4);

			art4.setCredit(fovc);
			// art4.setDebit(fovc);
			art4.setAccount(fovial);
			art4.setDuedate(parameters.getDocduedate());
			art4.setShortname(fovial);
			art4.setContraact(buss_c);
			art4.setLinememo(parameters.getJrnlmemo());
			art4.setRefdate(parameters.getDocdate());
			art4.setRef1(parameters.getRef1());
			// art2.setRef2();
			art4.setBaseref(parameters.getRef1());
			art4.setTaxdate(parameters.getTaxdate());
			// rt1.setFinncpriod(finncpriod);
			art4.setReltransid(-1);
			art4.setRellineid(-1);
			art4.setReltype("N");
			art4.setObjtype("5");
			art4.setVatline("N");
			art4.setVatamount(0.0);
			art4.setClosed("N");
			art4.setGrossvalue(0.0);
			art4.setBalduedeb(0.0);
			art4.setBalduecred(fovc);
			art4.setIsnet("Y");
			art4.setTaxtype(0);
			art4.setTaxpostacc("N");
			art4.setTotalvat(0.0);
			art4.setWtliable("N");
			art4.setWtline("N");
			art4.setPayblock("N");
			art4.setOrdered("N");
			art4.setTranstype(parameters.getObjtype());
			detail.add(art4);
		}
		if (cotrans != 0.0) {
			art5.setLine_id(5);

			art5.setCredit(cotrans);
			// art5.setDebit(cotrans);
			art5.setAccount(cotrans_C);
			art5.setDuedate(parameters.getDocduedate());
			art5.setShortname(cotrans_C);
			art5.setContraact(buss_c);
			art5.setLinememo(parameters.getJrnlmemo());
			art5.setRefdate(parameters.getDocdate());
			art5.setRef1(parameters.getRef1());
			// 5rt2.setRef2();
			art5.setBaseref(parameters.getRef1());
			art5.setTaxdate(parameters.getTaxdate());
			// 54rt1.setFinncpriod(finncpriod);
			art5.setReltransid(-1);
			art5.setRellineid(-1);
			art5.setReltype("N");
			art5.setObjtype("5");
			art5.setVatline("N");
			art5.setVatamount(0.0);
			art5.setClosed("N");
			art5.setGrossvalue(0.0);
			art5.setBalduedeb(0.0);
			art5.setBalduecred(cotrans);
			art5.setIsnet("Y");
			art5.setTaxtype(0);
			art5.setTaxpostacc("N");
			art5.setTotalvat(0.0);
			art5.setWtliable("N");
			art5.setWtline("N");
			art5.setPayblock("N");
			art5.setOrdered("N");
			art5.setTranstype(parameters.getObjtype());
			detail.add(art5);
		}
		nuevo.setJournalentryList(detail);

		nuevo = fill_JournalEntry_Unir2(nuevo);
		return nuevo;
	}

	public JournalEntryTO fill_JournalEntry_Unir2(JournalEntryTO parameters)
			throws Exception {
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
			Double saldo = sum_debe - sum_credit;
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
			} else {
				art1.setDebit(zero);
				art1.setBalduedeb(saldo);
				art1.setBalduecred(zero);
			}

			// --------------------------------------------------------------------------------------------------------------------------------------------------------
			// llenado del asiento contable
			// --------------------------------------------------------------------------------------------------------------------------------------------------------

			art1.setLine_id(n);
			art1.setAccount(acc);
			art1.setDuedate(parameters.getDuedate());
			art1.setShortname(acc);
			art1.setContraact(c_acc);
			art1.setLinememo(parameters.getMemo());
			art1.setRefdate(parameters.getRefdate());
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

	public JournalEntryTO fill_JournalEntry_pago(SupplierTO parameters)
			throws Exception {

		String buss_c;
		String branch_c;
		String iva_c;
		String V_local;
		String costo_venta;
		String fovialCotrans_c = null;
		double bussines = 0.0;
		double branch = 0.0;
		double tax = 0.0;
		double fovc = 0.0;
		double sale = 0.0;
		double costo = 0.0;
		double impuesto = 0.0;

		JournalEntryTO nuevo = new JournalEntryTO();
		ResultOutTO _result = new ResultOutTO();
		ArticlesTO arti = new ArticlesTO();
		boolean ind = false;
		Double total = zero;
		List list = parameters.getsupplierDetails();
		List aux = new Vector();
		List<List> listas = new Vector();
		List aux1 = new Vector();
		// recorre la lista de detalles
		AdminDAO admin = new AdminDAO();
		CatalogTO Catalog = new CatalogTO();
		Catalog = admin.findCatalogByKey("IVA", 12);

		ParameterDAO admin1 = new ParameterDAO();
		parameterTO Catalog1 = new parameterTO();
		Catalog1 = admin1.getParameterbykey(7);

		for (Object obj : list) {
			SupplierDetailTO good = (SupplierDetailTO) obj;
			String cod = good.getAcctcode();
			List lisHija = new Vector();
			// calculando los impuestos y saldo de las cuentas
			// --------------------------------------------------------------------------------

			arti = good.getArticle();
			branch = branch + (arti.getAvgPrice() * good.getQuantity());
			sale = sale + good.getLinetotal();
			// calculando el iva validando si el producto esta exento o de iva
			if (good.getTaxstatus().equals("Y")) {

				impuesto = good.getLinetotal()
						* (Double.parseDouble(Catalog.getCatvalue()) / 100);
				fovc = fovc + (good.getVatsum() - impuesto);
				tax = tax + impuesto;

			}
			bussines = bussines
					+ ((good.getVatsum() - impuesto) + impuesto + good
							.getLinetotal());
		}
		// consultando en la base de datos los codigos de cuenta asignados
		// cuenta de bussines partner
		buss_c = parameters.getCtlaccount();

		// asiento contable
		JournalEntryLinesTO art1 = new JournalEntryLinesTO();
		JournalEntryLinesTO art2 = new JournalEntryLinesTO();
		JournalEntryLinesTO art3 = new JournalEntryLinesTO();
		JournalEntryLinesTO art4 = new JournalEntryLinesTO();
		JournalEntryLinesTO art5 = new JournalEntryLinesTO();
		JournalEntryLinesTO art6 = new JournalEntryLinesTO();
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// llenado del asiento contable
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// LLenado del padre
		List detail = new Vector();
		nuevo.setObjtype("5");
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setLoctotal(bussines);
		nuevo.setSystotal(bussines);
		nuevo.setMemo("Anulación pago de factura de compra");
		nuevo.setUsersign(parameters.getUsersign());
		nuevo.setDuedate(parameters.getDocdate());
		nuevo.setTaxdate(parameters.getTaxdate());
		nuevo.setBtfstatus("O");
		nuevo.setTranstype(parameters.getObjtype());
		nuevo.setBaseref(parameters.getRef1());
		nuevo.setRefdate(parameters.getDocduedate());
		nuevo.setRef1(parameters.getRef1());
		nuevo.setRefndrprt("N");
		nuevo.setAdjtran("N");
		nuevo.setAutostorno("N");
		nuevo.setAutovat("N");
		nuevo.setPrinted("N");
		nuevo.setAutowt("N");
		nuevo.setDeferedtax("N");

		// llenado de los
		// hijos---------------------------------------------------------------------------------------------------
		// cuenta del socio de negocio
		art1.setLine_id(1);
		art1.setCredit(bussines);
		art1.setAccount(buss_c);
		art1.setDuedate(parameters.getDocduedate());
		art1.setShortname(buss_c);
		art1.setContraact(Catalog1.getValue1());
		art1.setLinememo("Anulación pago de factura de compra");
		art1.setRefdate(parameters.getDocdate());
		art1.setRef1(parameters.getRef1());
		// ar1.setRef2();
		art1.setBaseref(parameters.getRef1());
		art1.setTaxdate(parameters.getTaxdate());
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
		art1.setBalduecred(bussines);
		art1.setIsnet("Y");
		art1.setTaxtype(0);
		art1.setTaxpostacc("N");
		art1.setTotalvat(0.0);
		art1.setWtliable("N");
		art1.setWtline("N");
		art1.setPayblock("N");
		art1.setOrdered("N");
		art1.setTranstype(parameters.getObjtype());
		detail.add(art1);
		// ---------------------------------------------------------------------------------------------------
		// cuenta del pago del compra
		// ---------------------------------------------------------------------------------------------------
		art2.setLine_id(2);
		art2.setDebit(bussines);
		art2.setAccount(Catalog1.getValue1());
		art2.setDuedate(parameters.getDocduedate());
		art2.setShortname(Catalog1.getValue1());
		art2.setContraact(buss_c);
		art2.setLinememo("Anulación pago de factura de compra");
		art2.setRefdate(parameters.getDocdate());
		art2.setRef1(parameters.getRef1());
		// r1.setRef2();
		art2.setBaseref(parameters.getRef1());
		art2.setTaxdate(parameters.getTaxdate());
		// art1.setFinncpriod(finncpriod);
		art2.setReltransid(-1);
		art2.setRellineid(-1);
		art2.setReltype("N");
		art2.setObjtype("5");
		art2.setVatline("N");
		art2.setVatamount(0.0);
		art2.setClosed("N");
		art2.setGrossvalue(0.0);
		art2.setBalduedeb(bussines);
		art2.setBalduecred(0.0);
		art2.setIsnet("Y");
		art2.setTaxtype(0);
		art2.setTaxpostacc("N");
		art2.setTotalvat(0.0);
		art2.setWtliable("N");
		art2.setWtline("N");
		art2.setPayblock("N");
		art2.setOrdered("N");
		art2.setTranstype(parameters.getObjtype());
		detail.add(art2);

		nuevo.setJournalentryList(detail);
		return nuevo;
	}

	public PurchaseTO update_lines(PurchaseTO purchase, SupplierTO supplier) {
		// -----------------------------------------------------------------
		// inicializacion de DAtos
		// -----------------------------------------------------------------
		List supdetail = new Vector();
		List purdetail = new Vector();
		supdetail = supplier.getsupplierDetails();
		purdetail = purchase.getpurchaseDetails();
		int c_purch = purdetail.size();
		int c_supp = supdetail.size();
		// -----------------------------------------------------------------
		// compara si ambas listas de destalles son iguales o menor
		// -----------------------------------------------------------------
		if (c_supp < c_purch) {
			// si es menor da comienzo a anulacion por linea
			// recorrido por detalles de nota de credito
			for (Object obj : supdetail) {
				SupplierDetailTO good = (SupplierDetailTO) obj;
				// recorrido por lista de detalles de venta
				for (Object obj1 : purdetail) {
					PurchaseDetailTO good1 = (PurchaseDetailTO) obj;
					if (good.getBaseline() == good1.getLinenum()) {
						good1.setLinestatus("C");
					}

				}

			}
		} else {
			// si es mayor o igual se anula documento completo
			purchase.setCanceled("Y");
			purchase.setDocstatus("C");
		}
		return purchase;

	}

	// -----------------------------------------------------------------------------------------------------------------
	//
	// -----------------------------------------------------------------------------------------------------------------
	public List getSupplier(SupplierInTO param) throws Exception {
		// TODO Auto-generated method stub
		List _return;
		SupplierDAO DAO = new SupplierDAO();

		try {
			_return = DAO.getSupplier(param);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public SupplierTO getSupplierByKey(int docentry) throws Exception {
		// TODO Auto-generated method stub
		SupplierTO _return = new SupplierTO();
		SupplierDAO DAO = new SupplierDAO();
		try {
			_return = DAO.getSupplierByKey(docentry);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}

		return _return;
	}

	public List getSupplierDetail(int docentry) throws Exception {
		// TODO Auto-generated method stub
		List _return;
		SupplierDetailDAO DAO = new SupplierDetailDAO();

		try {
			_return = DAO.getsupplierDetail(docentry);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (EJBException) new EJBException(e);
		}
		return _return;
	}

	public ResultOutTO validate_inv_supplier_mtto(SupplierTO parameters)
			throws EJBException {
		boolean valid = false;
		ResultOutTO _return = new ResultOutTO();
		AccountingEJB acc = new AccountingEJB();
		CatalogEJB Businesspartner = new CatalogEJB();
		AdminEJB EJB1 = new AdminEJB();
		List branch = new Vector();
		ArticlesTO DBArticle = new ArticlesTO();
		String code;
		// ------------------------------------------------------------------------------------------------------------
		// validaciones
		// ------------------------------------------------------------------------------------------------------------

		// ------------------------------------------------------------------------------------------------------------
		// Validación almacen bloqueado
		// ------------------------------------------------------------------------------------------------------------
		if (parameters.getTowhscode() == null) {
			_return.setCodigoError(1);
			_return.setMensaje("Codigo de almacen null");

			return _return;
		}
		_return = EJB1.validate_branchActiv(parameters.getTowhscode());

		if (_return.getCodigoError() != 0) {
			_return.setCodigoError(1);
			_return.setMensaje("El Almacen no esta activo");

			return _return;
		}
		// ------------------------------------------------------------------------------------------------------------
		// Validación de fecha de periodo contable
		// ------------------------------------------------------------------------------------------------------------
		if (parameters.getDocdate() == null) {
			_return.setCodigoError(1);
			_return.setMensaje("no se encuentra fecha");

			return _return;
		}
		_return = acc.validate_exist_accperiod(parameters.getDocdate());
		if (_return.getCodigoError() != 0) {
			_return.setCodigoError(1);
			_return.setMensaje("El documento tiene una fecha Fuera del periodo contable activo");
			return _return;
		}
		// ------------------------------------------------------------------------------------------------------------
		// Validación del socio de negocio
		// ------------------------------------------------------------------------------------------------------------
		if (parameters.getCardcode() == null) {
			_return.setCodigoError(1);
			_return.setMensaje("Codigo de socio de negocio null");

			return _return;
		}
		_return = Businesspartner.validate_businesspartnerBykey(parameters
				.getCardcode());
		if (_return.getCodigoError() != 0) {
			_return.setCodigoError(1);
			_return.setMensaje("el socio de negocio no esta activo para esta transaccion");
			return _return;
		}

		// recorre el ClientCrediDetail
		Iterator<SupplierDetailTO> iterator1 = parameters.getsupplierDetails()
				.iterator();
		while (iterator1.hasNext()) {
			AdminEJB EJB = new AdminEJB();
			// Consultar información actualizada desde la base
			SupplierDetailTO SupplierDetail = (SupplierDetailTO) iterator1
					.next();
			code = SupplierDetail.getItemcode();

			DBArticle = EJB.getArticlesByKey(code);

			// ------------------------------------------------------------------------------------------------------------
			// Validación articulo existe
			// ------------------------------------------------------------------------------------------------------------
			valid = false;
			if (DBArticle != null) {
				valid = true;
			}

			if (!valid) {
				_return.setLinenum(SupplierDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ SupplierDetail.getItemcode() + " "
						+ SupplierDetail.getDscription()

						+ " no existe,informar al administrador. linea :"
						+ SupplierDetail.getLinenum());

				return _return;

			}

			// ------------------------------------------------------------------------------------------------------------
			// Validación articulo activo
			// ------------------------------------------------------------------------------------------------------------

			valid = false;
			if (DBArticle.getValidFor() != null
					&& DBArticle.getValidFor().toUpperCase().equals("Y")) {
				valid = true;
			}

			if (!valid) {
				_return.setLinenum(SupplierDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ SupplierDetail.getItemcode() + " "
						+ SupplierDetail.getDscription()

						+ " No esta activo. linea :"
						+ SupplierDetail.getLinenum());

				return _return;

			}

			// ------------------------------------------------------------------------------------------------------------
			// Validación articulo de compra
			// ------------------------------------------------------------------------------------------------------------
			valid = false;
			if (DBArticle.getPrchseItem() != null
					&& DBArticle.getPrchseItem().toUpperCase().equals("Y")) {
				valid = true;
			}

			if (!valid) {
				_return.setLinenum(SupplierDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ SupplierDetail.getItemcode() + " "
						+ SupplierDetail.getDscription()
						+ " No es un articulo de venta. linea :"
						+ SupplierDetail.getLinenum());
				return _return;
			}

			// ------------------------------------------------------------------------------------------------------------
			// Validación almacen bloqueado para articulo
			// ------------------------------------------------------------------------------------------------------------
			valid = false;

			branch = DBArticle.getBranchArticles();

			for (Object object : branch) {
				BranchArticlesTO branch1 = (BranchArticlesTO) object;

				if (branch1.getWhscode().equals(SupplierDetail.getWhscode())) {
					if (branch1.isIsasociated() && branch1.getLocked() != null
							&& branch1.getLocked().toUpperCase().equals("F")) {
						valid = true;
					}
				}
			}

			if (!valid) {
				_return.setLinenum(SupplierDetail.getLinenum());
				_return.setCodigoError(1);
				_return.setMensaje("El articulo "
						+ SupplierDetail.getItemcode()
						+ " "
						+ SupplierDetail.getDscription()
						+ " No esta asignado o esta bloquedo para el almacen indicado. linea :"
						+ SupplierDetail.getLinenum());
				return _return;
			}

		}
		_return.setCodigoError(0);

		return _return;

	}

}
