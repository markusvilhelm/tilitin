package kirjanpito.reports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kirjanpito.db.Account;
import kirjanpito.db.DTOCallback;
import kirjanpito.db.DataAccessException;
import kirjanpito.db.DataSource;
import kirjanpito.db.Document;
import kirjanpito.db.Entry;
import kirjanpito.db.Period;
import kirjanpito.db.Session;
import kirjanpito.db.Settings;
import kirjanpito.util.AccountBalances;

/**
 * Malli tiliotetulosteelle.
 * 
 * @author Tommi Helineva
 */
public class AccountStatementModel implements PrintModel {
	private DataSource dataSource;
	private Settings settings;
	private Period period;
	private Account account;
	private AccountStatementRow[] rows;
	private BigDecimal debitTotal;
	private BigDecimal kreditTotal;
	private Date startDate;
	private Date endDate;
	private AccountBalances balances;
	
	/**
	 * Palauttaa tietokannan, josta tiedot haetaan.
	 * 
	 * @return tietokanta
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Asettaa tietokannan, josta tiedot haetaan.
	 * 
	 * @param dataSource tietokanta
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Palauttaa asetukset.
	 * 
	 * @return asetukset
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * Asettaa asetukset.
	 * 
	 * @param settings asetukset
	 */
	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	/**
	 * Palautaa tilikauden, jonka tilitapahtumat haetaan.
	 * 
	 * @return tilikausi
	 */
	public Period getPeriod() {
		return period;
	}

	/**
	 * Asettaa tilikauden, jonka tilitapahtumat haetaan.
	 * 
	 * @param period tilikausi
	 */
	public void setPeriod(Period period) {
		this.period = period;
	}
	
	/**
	 * Palauttaa alkamispäivämäärän.
	 * 
	 * @return alkamispäivämäärä
	 */
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * Asettaa alkamispäivämäärän.
	 * 
	 * @param startDate alkamispäivämäärä
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * Palauttaa päättymispäivämäärän.
	 * 
	 * @return päättymispäivämäärä
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Asettaa päättymispäivämäärän.
	 * 
	 * @param endDate päättymispäivämäärä
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * Palauttaa tilin, jonka tapahtumat haetaan.
	 * 
	 * @return tili
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * Asettaa tilin, jonka tapahtumat haetaan.
	 * 
	 * @param account tili
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	public void run() throws DataAccessException {
		Session sess = null;
		final ArrayList<AccountStatementRow> rowList = new ArrayList<AccountStatementRow>();
		balances = new AccountBalances();
		balances.addAccount(account);
		debitTotal = BigDecimal.ZERO;
		kreditTotal = BigDecimal.ZERO;
		
		/* Haetaan tilikauden tositteet ja viennit. */
		try {
			sess = dataSource.openSession();
			List<Document> documents = dataSource.getDocumentDAO(
					sess).getByPeriodIdAndDate(period.getId(), startDate, endDate);
			
			/* Luodaan tositteista hajautustaulu, jotta ne
			 * löytyvät nopeasti tunnisteen perusteella. */
			final HashMap<Integer, Document> documentMap = new HashMap<Integer, Document>();
			
			for (Document d : documents) {
				documentMap.put(d.getId(), d);
			}
			
			dataSource.getEntryDAO(sess).getByPeriodIdAndAccountId(
					period.getId(), account.getId(),
					new DTOCallback<Entry>() {
						public void process(Entry entry) {
							balances.addEntry(entry);
							Document document = documentMap.get(entry.getDocumentId());
							
							if (document == null) {
								return;
							}
							
							if (document.getNumber() >= 1) {
								if (entry.isDebit()) {
									debitTotal = debitTotal.add(entry.getAmount());
								}
								else {
									kreditTotal = kreditTotal.add(entry.getAmount());
								}
							}
							
							rowList.add(new AccountStatementRow(
									document.getNumber(), document.getDate(),
									entry, balances.getBalance(account.getId())));
						}
					});
		}
		finally {
			if (sess != null) sess.close();
		}
		
		rowList.add(new AccountStatementRow(-1, null, null,
				balances.getBalance(account.getId())));
		rows = new AccountStatementRow[rowList.size()];
		rowList.toArray(rows);
	}
	
	/**
	 * Palauttaa käyttäjän nimen.
	 * 
	 * @return käyttäjän nimi
	 */
	public String getName() {
		return settings.getName();
	}
	
	/**
	 * Palauttaa Y-tunnuksen.
	 * 
	 * @return y-tunnus
	 */
	public String getBusinessId() {
		return settings.getBusinessId();
	}
	
	/**
	 * Palauttaa tulosteessa olevien rivien lukumäärän.
	 * 
	 * @return rivien lukumäärä
	 */
	public int getRowCount() {
		return rows.length;
	}
	
	/**
	 * Palauttaa vientien lukumäärän.
	 * 
	 * @return vientien lukumäärä
	 */
	public int getEntryCount() {
		return rows.length - 1;
	}
	
	/**
	 * Palauttaa rivillä <code>index</code> olevan
	 * tilitapahtuman tositenumeron.
	 * 
	 * @param index rivinumero
	 * @return tositenumero
	 */
	public int getDocumentNumber(int index) {
		return rows[index].documentNumber;
	}
	
	/**
	 * Palauttaa rivillä <code>index</code> olevan
	 * tilitapahtuman päivämäärän.
	 * 
	 * @param index rivinumero
	 * @return päivämäärä
	 */
	public Date getDate(int index) {
		return rows[index].date;
	}
	
	/**
	 * Palauttaa rivillä <code>index</code> olevan
	 * tilitapahtuman viennin.
	 * 
	 * @param index rivinumero
	 * @return vienti
	 */
	public Entry getEntry(int index) {
		return rows[index].entry;
	}
	
	/**
	 * Palauttaa tilin saldon rivillä <code>index</code> olevan
	 * tilitapahtuman jälkeen.
	 * 
	 * @param index rivinumero
	 * @return tilin saldo
	 */
	public BigDecimal getBalance(int index) {
		return rows[index].balance;
	}
	
	/**
	 * Palauttaa debet-vientien summan.
	 * 
	 * @return summa
	 */
	public BigDecimal getDebitTotal() {
		return debitTotal;
	}

	/**
	 * Palauttaa kredit-vientien summan.
	 * 
	 * @return summa
	 */
	public BigDecimal getKreditTotal() {
		return kreditTotal;
	}
	
	private class AccountStatementRow {
		public int documentNumber;
		public Date date;
		public Entry entry;
		public BigDecimal balance;
		
		public AccountStatementRow(int documentNumber, Date date,
				Entry entry, BigDecimal balance)
		{
			this.documentNumber = documentNumber;
			this.date = date;
			this.entry = entry;
			this.balance = balance;
		}
	}
}
