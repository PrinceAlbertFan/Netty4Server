package global;

import java.io.FileReader;
import java.util.Properties;

public final class GloConst {
	
	public static final short SERVER_ID;
	public static final short LOGIC_SERVER_ID;
	public static final short BATTLE_SERVER_ID;
	public static final short CHAT_SERVER_ID;
	public static final short GUILD_SERVER_ID;
	public static final short MAIL_SERVER_ID;
	
	static {
		Properties properties = new Properties();
		try {
			FileReader reader = new FileReader("config/config.properties");
			properties.load(reader);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		SERVER_ID = Short.parseShort(properties.getProperty("SERVER_ID"));
		LOGIC_SERVER_ID = Short.parseShort(properties.getProperty("LOGIC_SERVER_ID"));
		BATTLE_SERVER_ID = Short.parseShort(properties.getProperty("BATTLE_SERVER_ID"));
		CHAT_SERVER_ID = Short.parseShort(properties.getProperty("CHAT_SERVER_ID"));
		GUILD_SERVER_ID = Short.parseShort(properties.getProperty("GUILD_SERVER_ID"));
		MAIL_SERVER_ID = Short.parseShort(properties.getProperty("MAIL_SERVER_ID"));
		properties.clear();
	}
}
