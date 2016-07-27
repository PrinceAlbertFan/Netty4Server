package tool;

import java.io.UTFDataFormatException;

import io.netty.buffer.ByteBuf;

public final class ByteBufDecoder {
	
	public static String getString(ByteBuf buffer, int arg0) {
		int utflen = buffer.getUnsignedShort(arg0);
		if (utflen > 0) {
			if (utflen > buffer.writerIndex() - arg0 - 2) {
				return null;
			}
			byte[] byteArr = new byte[utflen];
			char[] charArr = new char[utflen];
			int byteArrCount = 0, charArrCount = 0, c, b;
			byte b1, b2;
			
			buffer.getBytes(arg0 + 2, byteArr, 0, utflen);

			while (byteArrCount < utflen) {
				c = byteArr[byteArrCount++] & 0xFF;
				b = c >> 4;
				if (b >= 0x00 && b < 0x08) {/* 0xxxxxxx */
					charArr[charArrCount++] = (char) c;
				} else if (b == 0x0C || b == 0x0D) {/* 110x xxxx 10xx xxxx */
					/*
					 * if (++byteArrCount > utflen) { throw new
					 * UTFDataFormatException
					 * ("malformed input: partial character at end"); }
					 */
					b1 = byteArr[byteArrCount++];
					/*
					 * if ((b1 & 0xC0) != 0x80) { throw new
					 * UTFDataFormatException("malformed input around byte " +
					 * byteArrCount); }
					 */
					charArr[charArrCount++] = (char) ((c & 0x1F) << 6 | b1 & 0x3F);
				} else if (b == 0x0E) {/* 1110 xxxx 10xx xxxx 10xx xxxx */
					/*
					 * if ((byteArrCount += 2) > utflen) { throw new
					 * UTFDataFormatException
					 * ("malformed input: partial character at end"); }
					 */
					b1 = byteArr[byteArrCount++];
					b2 = byteArr[byteArrCount++];
					/*
					 * if ((b1 & 0xC0) != 0x80 || (b2 & 0xC0) != 0x80) { throw
					 * new UTFDataFormatException("malformed input around byte "
					 * + (byteArrCount - 1)); }
					 */
					charArr[charArrCount++] = (char) ((c & 0x0F) << 12 | (b1 & 0x3F) << 6 | b2 & 0x3F);
				} else {/* 10xx xxxx, 1111 xxxx */
					/*try {
						throw new UTFDataFormatException("malformed input around byte " + byteArrCount);
					} catch (UTFDataFormatException e) {
						e.printStackTrace();
					}*/
					return null;
				}
			}
			// The number of chars produced may be less than utflen
			return new String(charArr, 0, charArrCount);
		}

		return "";
	}
	
	public static String readString(ByteBuf buffer) {
		int utflen = buffer.readUnsignedShort();
		if (utflen > 0) {
			if (utflen > buffer.readableBytes()) {
				return null;
			}
			byte[] byteArr = new byte[utflen];
			char[] charArr = new char[utflen];
			int byteArrCount = 0, charArrCount = 0, c, b;
			byte b1, b2;

			buffer.readBytes(byteArr, 0, utflen);

			while (byteArrCount < utflen) {
				c = byteArr[byteArrCount++] & 0xFF;
				b = c >> 4;
				if (b >= 0x00 && b < 0x08) {/* 0xxxxxxx */
					charArr[charArrCount++] = (char) c;
				} else if (b == 0x0C || b == 0x0D) {/* 110x xxxx 10xx xxxx */
					/*
					 * if (++byteArrCount > utflen) { throw new
					 * UTFDataFormatException
					 * ("malformed input: partial character at end"); }
					 */
					b1 = byteArr[byteArrCount++];
					/*
					 * if ((b1 & 0xC0) != 0x80) { throw new
					 * UTFDataFormatException("malformed input around byte " +
					 * byteArrCount); }
					 */
					charArr[charArrCount++] = (char) ((c & 0x1F) << 6 | b1 & 0x3F);
				} else if (b == 0x0E) {/* 1110 xxxx 10xx xxxx 10xx xxxx */
					/*
					 * if ((byteArrCount += 2) > utflen) { throw new
					 * UTFDataFormatException
					 * ("malformed input: partial character at end"); }
					 */
					b1 = byteArr[byteArrCount++];
					b2 = byteArr[byteArrCount++];
					/*
					 * if ((b1 & 0xC0) != 0x80 || (b2 & 0xC0) != 0x80) { throw
					 * new UTFDataFormatException("malformed input around byte "
					 * + (byteArrCount - 1)); }
					 */
					charArr[charArrCount++] = (char) ((c & 0x0F) << 12 | (b1 & 0x3F) << 6 | b2 & 0x3F);
				} else {/* 10xx xxxx, 1111 xxxx */
					/*try {
						throw new UTFDataFormatException("malformed input around byte " + byteArrCount);
					} catch (UTFDataFormatException e) {
						e.printStackTrace();
					}*/
					return null;
				}
			}
			// The number of chars produced may be less than utflen
			return new String(charArr, 0, charArrCount);
		}

		return "";
	}
	
	public static void writeString(ByteBuf buf, String str) {
		byte[] byteArr;
		int utflen = 0;
		
		if (str.isEmpty()) {
			byteArr = new byte[2];
		} else {
			int strlen = str.length(), count = 0, i;
			char c;
			
			/* use charAt instead of copying String to char array */
			for (i = 0; i < strlen; ++i) {
				c = str.charAt(i);
				if (c > 0x00 && c < 0x80) {
					++utflen;
				}
				else if (c > 0x07FF) {
					utflen += 3;
				}
				else {
					utflen += 2;
				}
			}

			/*if (utflen > 0xFFFF) {
				try {
					throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");
				} catch (UTFDataFormatException e) {
					e.printStackTrace();
				}
			}*/
			
			byteArr = new byte[utflen + 2];

			byteArr[count++] = (byte) (utflen >> 8);
			byteArr[count++] = (byte) (utflen & 0xFF);
			
			for (i = 0; i < strlen; ++i) {
				c = str.charAt(i);
				if (c > 0x00 && c < 0x80) {
					byteArr[count++] = (byte) c;
				}
				else if (c > 0x07FF) {
					byteArr[count++] = (byte) (0xE0 | c >> 12);
					byteArr[count++] = (byte) (0x80 | c >> 6 & 0x3F);
					byteArr[count++] = (byte) (0x80 | c & 0x3F);
				}
				else {
					byteArr[count++] = (byte) (0xC0 | c >> 6 & 0x1F);
					byteArr[count++] = (byte) (0x80 | c & 0x3F);
				}
			}
		}
		
		buf.writeBytes(byteArr, 0, utflen + 2);
	}
}
