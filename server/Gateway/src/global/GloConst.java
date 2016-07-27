package global;

import java.io.FileReader;
import java.util.Properties;

public final class GloConst {
	
	public static final short MAIN_SERVER_ID;
	public static final short BATTLE_SERVER_ID;
	public static final short CHAT_SERVER_ID;
	public static final short LOGIN_SERVER_ID;
	public static final short GUILD_SERVER_ID;
	public static final short MAIL_SERVER_ID;
	
	static {
		Properties properties = new Properties();
		FileReader reader = null;
		try {
			reader = new FileReader("config/config.properties");
			properties.load(reader);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		MAIN_SERVER_ID = Short.parseShort(properties.getProperty("SERVER_ID_MAIN"));
		BATTLE_SERVER_ID = Short.parseShort(properties.getProperty("SERVER_ID_BATTLE"));
		CHAT_SERVER_ID = Short.parseShort(properties.getProperty("SERVER_ID_CHAT"));
		LOGIN_SERVER_ID = Short.parseShort(properties.getProperty("SERVER_ID_LOGIN"));
		GUILD_SERVER_ID = Short.parseShort(properties.getProperty("SERVER_ID_GUILD"));
		MAIL_SERVER_ID = Short.parseShort(properties.getProperty("SERVER_ID_MAIL"));
		properties.clear();
	}
}
