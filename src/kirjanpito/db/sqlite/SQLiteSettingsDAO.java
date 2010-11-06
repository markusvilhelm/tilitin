package kirjanpito.db.sqlite;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import kirjanpito.db.sql.SQLSettingsDAO;

/**
 * <code>SQLiteSettingsDAO</code>:n avulla voidaan hakea ja tallentaa
 * asetukset.
 * 
 * @author Tommi Helineva
 */
public class SQLiteSettingsDAO extends SQLSettingsDAO {
	private SQLiteSession sess;
	
	/**
	 * Luo <code>SQLiteSettingsDAO</code>-olion, joka käyttää
	 * tietokantaistuntoa <code>sess</code>
	 * 
	 * @param sess tietokantaistunto
	 */
	public SQLiteSettingsDAO(SQLiteSession sess) {
		this.sess = sess;
	}
	
	/**
	 * Palauttaa SELECT-kyselyn, jonka avulla asetukset haetaan
	 * tietokannasta. Kysely palauttaa vain yhden rivin.
	 * 
	 * @return SELECT-kysely
	 * @throws SQLException jos kyselyn luominen epäonnistuu
	 */
	protected PreparedStatement getSelectQuery() throws SQLException {
		return sess.prepareStatement("SELECT name, business_id, current_period_id, document_type_id, properties FROM settings");
	}
	
	/**
	 * Palauttaa INSERT-kyselyn, jonka avulla rivi lisätään.
	 * 
	 * @return INSERT-kysely
	 * @throws SQLException jos kyselyn luominen epäonnistuu
	 */
	protected PreparedStatement getInsertQuery() throws SQLException {
		return sess.prepareStatement("INSERT INTO settings (version, name, business_id, current_period_id, document_type_id, properties) VALUES (9, ?, ?, ?, ?, ?)");
	}
	
	/**
	 * Palauttaa UPDATE-kyselyn, jonka avulla rivin kaikki kentät päivitetään.
	 * 
	 * @return UPDATE-kysely
	 * @throws SQLException jos kyselyn luominen epäonnistuu
	 */
	protected PreparedStatement getUpdateQuery() throws SQLException {
		return sess.prepareStatement("UPDATE settings SET name=?, business_id=?, current_period_id=?, document_type_id=?, properties=?");
	}
}
