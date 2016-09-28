import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;

public class DBOutputWritable implements Writable, DBWritable {
	private String starting_phrase;
	private String following_word;
	private int count;

	public DBOutputWritable(String starting_phrase, String following_word, int count) {
		this.starting_phrase = starting_phrase;
		this.following_word = following_word;
		this.count = count;
	}

	@Override
	public void readFields(ResultSet rs) throws SQLException {
		starting_phrase = rs.getString(1);
		following_word = rs.getString(2);
		count = rs.getInt(3);

	}

	@Override
	public void write(PreparedStatement ps) throws SQLException {
		ps.setString(1, starting_phrase);
		ps.setString(2, following_word);
		ps.setInt(3, count);

	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		// TODO Auto-generated method stub

	}
}