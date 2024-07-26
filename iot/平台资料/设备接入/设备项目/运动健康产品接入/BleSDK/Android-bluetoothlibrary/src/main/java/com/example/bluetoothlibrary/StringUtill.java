package com.example.bluetoothlibrary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

public class StringUtill {

	public static String checkString(String string) {
		boolean isNum = false, isInfo = true;
		String[] temp = string.split("/(.*)\r\n|\n|\r/g");
		StringBuffer resoult = new StringBuffer();
		// Log.v("test", "temp.length:" + temp.length);
		if (temp.length > 1) {
			resoult.append("<div class='section_content'>");
			for (String str : temp) {
				// Log.d("test", "str:" + str);

				String reg1 = "^\\d.*?$"; // ^表示字符串开始，\\d表示数字
				if (Pattern.matches(reg1, str)) {
					resoult.append("<div class='section_title'><p>" + str + "<p></div>");
					// Log.d("test", "str:" + str);
				} else if (str.startsWith("●")) {
					resoult.append("<div class='section_step'><p>" + str + "<p></div>");
					// Log.d("test", "str:" + str);
				} else {
					resoult.append("<div class='section_info'><p>" + str + "<p></div>");
				}
			}
			resoult.append("</div>");
		} else {

			resoult.append("<div class='section_info'>");
			resoult.append("<p>");
			resoult.append(string);
			resoult.append("</p>");
			resoult.append("</div>");
			// Log.v("test",resoult.toString());
			// Log.v("test", "str:" + resoult.toString());
		}

		return resoult.toString();

	}

	/**
	 * 获取当前时间来命名，以免有重复的文件名,再加上5位的随机数
	 * 
	 * @return
	 */
	public static String getTimeName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmmssSSS");
		String timeName = dateFormat.format(date) + "_" + new Random().nextInt(99999);
		return timeName;
	}

	public static String getTimeNameSql() {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddhhmmssSSS");
		String timeName = dateFormat.format(date) + df.format(new Random().nextInt(999));
		return timeName;
	}

	/**
	 * 将 8/29拿出两位数字
	 */
	public static int[] getIntByString(String str) {
		String[] bgStrs = str.split("\\.");
		int[] bgs = new int[bgStrs.length];
		for (int i = 0; i < bgStrs.length; i++) {
			bgs[i] = Integer.valueOf(bgStrs[i]);
		}
		return bgs;
	}

	public static String bytesToHexString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static long strtoul(String str,int base){
		return strtoul((str+"\0").toCharArray(),base);
	}

    private static long strtoul(char[] cp,int base){
        long result=0,value;
        int i=0;
        if(base==0){
            base=10;
            if(cp[i]=='0'){
                base=8;
                i++;
                if(Character.toLowerCase(cp[i])=='x'&&isxdigit(cp[1])){
                    i++;
                    base=16;
                }
            }
        }else if(base==16){
            if(cp[0]=='0'&&Character.toLowerCase(cp[1])=='x')
                i+=2;
        }
        while(isxdigit(cp[i])&&(value = isdigit(cp[i]) ? cp[i]-'0' : Character.toLowerCase(cp[i])-'a'+10) < base){
            result=result*base+value;
            i++;
        }
        return result;
    }

    private static boolean isxdigit(char c){
        return ('0' <= c && c <= '9')||('a' <= c && c <= 'f')||('A' <= c && c <= 'F');
    }
    private static boolean isdigit(char c){
        return '0' <= c && c <= '9';
    }

	public static String hexString2binaryString(String hexString)
	{
		if (hexString == null || hexString.length() % 2 != 0)
			return null;
		String bString = "", tmp;
		for (int i = 0; i < hexString.length(); i++)
		{
			tmp = "0000"
					+ Integer.toBinaryString(Integer.parseInt(hexString
					.substring(i, i + 1), 16));
			bString += tmp.substring(tmp.length() - 4);
		}
		return bString;
	}

}
