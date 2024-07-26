package com.example.bluetoothlibrary;

import android.content.Context;
import android.widget.Toast;

import com.example.bluetoothlibrary.entity.Peripheral;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by apple on 16/7/23.
 */
public class DataCheckUtil extends Toast {
    public DataCheckUtil(Context context) {
        super(context);
    }

    public static boolean checkData(String data) {
        boolean result = true;
        String str = data;
        Pattern pattern = Pattern.compile(
                "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        if (!matcher.matches()) {
            result = false;
        }
        return result;
    }

    /** 判断邮箱格式 */
    public static boolean checkUri(String data) {
        boolean result = true;
        String str = data;
        Pattern pattern = Pattern
                .compile(
                        "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\){1}(([\\w-])+[.]){1,3}(net|com|cn|org|cc|tv|[0-9]{1,3})(\\S*\\/)((\\S)+[.]{1}(html|swf|jsp|php|jpg|gif|bmp|png)))",
                        Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        if (!matcher.matches()) {
            result = false;
        }
        return result;
    }


    public static boolean checkMainData(String data, int min, int max) {
        boolean result = false;
        String str = data;
        Pattern pattern = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{" + min + "," + max + "}$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            result = true;
        }
        return result;
    }

    /** 是否字母或数字 */
    public static boolean checkMainData2(String data) {
        boolean result = false;
        String str = data;
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            result = true;
        }
        return result;
    }

    // 判断血压
    public static boolean checkBloodPressureArea(String data) {
        boolean result = false;
        Pattern pattern = Pattern.compile("1\\d\\d", Pattern.CASE_INSENSITIVE);
        Pattern pattern1 = Pattern.compile("[1-9]\\d", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(data);
        Matcher matcher1 = pattern1.matcher(data);
        if (matcher.matches() || matcher1.matches()) {
            result = true;
        }

        return result;
    }


    public static boolean checkBloodSugarArea(String data) {
        boolean result = false;
        Pattern pattern5 = Pattern.compile("3[0-2]\\.\\d", Pattern.CASE_INSENSITIVE);
        Pattern pattern4 = Pattern.compile("33\\.[0]", Pattern.CASE_INSENSITIVE);
        Pattern pattern3 = Pattern.compile("3[0-3]{1}", Pattern.CASE_INSENSITIVE);
        Pattern pattern = Pattern.compile("[1-2]{1}\\d", Pattern.CASE_INSENSITIVE);
        Pattern pattern0 = Pattern.compile("[1-2]{1}\\d\\.\\d", Pattern.CASE_INSENSITIVE);
        Pattern pattern1 = Pattern.compile("[0-9].\\d", Pattern.CASE_INSENSITIVE);
        Pattern pattern2 = Pattern.compile("[1-9]{1}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(data);
        Matcher matcher0 = pattern0.matcher(data);
        Matcher matcher1 = pattern1.matcher(data);
        Matcher matcher2 = pattern2.matcher(data);
        Matcher matcher3 = pattern3.matcher(data);
        Matcher matcher4 = pattern4.matcher(data);
        Matcher matcher5 = pattern5.matcher(data);
        if (matcher.matches() || matcher0.matches() || matcher1.matches() || matcher2.matches()
                || matcher3.matches() || matcher4.matches() || matcher5.matches()) {
            result = true;
        }
        return result;
    }

    // 转换空格
    public static String replaceNull(String str) {
        return str;
    }

    // 转换回车符
    public static String consertTo(String str) {
        return str;
    }

    public static String stringFormat(String url) {
        String temp = url;

        // 替换回车
        // temp=temp.replaceAll("\n\n", "aa").replaceAll("\n",
        // "aa").replaceAll("\r", "aa").replaceAll("\r\n",
        // "aa").replaceAll("<br>", "aa");
        temp = temp.replaceAll("/(.*)\r\n|\n|\r/g", "aa");

        // 判断%
        if (url.contains("%")) {
            Pattern p = Pattern.compile("\\%");// 利用正则表达式检查空格空白字符
            Matcher m = p.matcher(temp);// 匹配
            temp = m.replaceAll("%25");// 替换所有的%为"%25"

        }
        // 判断#
        if (url.contains("#")) {
            Pattern p = Pattern.compile("\\#");// 利用正则表达式检查空格空白字符
            Matcher m = p.matcher(temp);// 匹配
            temp = m.replaceAll("%23");// 替换所有的#为"%23"

        }
        // 判断"
        if (url.contains("\"")) {
            Pattern p = Pattern.compile("\\\"");// 利用正则表达式检查
            Matcher m = p.matcher(temp);// 匹配
            temp = m.replaceAll("%22");// 替换所有的"为"%22"

        }

        // 判断\
        if (url.contains("\\")) {
            Pattern p = Pattern.compile("\\\\");// 利用正则表达式检查
            Matcher m = p.matcher(temp);// 匹配
            temp = m.replaceAll("%5C");// 替换所有的\为"%5C"
        }
        // 判断+
        if (url.contains("+")) {
            Pattern p = Pattern.compile("\\+");// 利用正则表达式检查
            Matcher m = p.matcher(temp);// 匹配
            temp = m.replaceAll("%2B");// 替换所有的+为"%2B"

        }
        // 判断^
        if (url.contains("^")) {
            Pattern p = Pattern.compile("\\^");// 利用正则表达式检查
            Matcher m = p.matcher(temp);// 匹配
            temp = m.replaceAll("%5E");// 替换所有的^为"%5E"

        }
        // 判断‘
        if (url.contains("`")) {
            Pattern p = Pattern.compile("\\`");// 利用正则表达式检查
            Matcher m = p.matcher(temp);// 匹配
            temp = m.replaceAll("%60");// 替换所有的^为"%5E"

        }

        // 判断<
        if (url.contains("<")) {
            Pattern p = Pattern.compile("\\<");// 利用正则表达式检查
            Matcher m = p.matcher(temp);// 匹配
            temp = m.replaceAll("%3C");// 替换所有的^为"%5E"
        }

        // 判断>
        if (url.contains(">")) {
            Pattern p = Pattern.compile("\\>");// 利用正则表达式检查
            Matcher m = p.matcher(temp);// 匹配
            temp = m.replaceAll("%3E");// 替换所有的^为"%5E"
        }

        // 判断空格
        String[] str = temp.split("\\?");
        if (str.length > 1) {
            String content = temp.substring(str[0].length() + 1);
            // Pattern p = Pattern.compile("\\s*|\t");// 利用正则表达式检查
            // Matcher m = p.matcher(content);// 匹配
            // content= m.replaceAll("%2O");// 替换所有的空格为""
            content = content.replaceAll(" ", "+");
            content = content.replaceAll("　", "+");
            temp = str[0] + "?" + content;
        }
        // Log.d("test", "temp:"+temp);
        return temp;

    }

    //
    public static String removeNull(String str) {
        return str.replace("null", "");
    }


    public static String convertTo(String str) {
        // 判断&
        if (str.contains("&")) {
            Pattern p = Pattern.compile("\\&");// 利用正则表达式检查
            Matcher m = p.matcher(str);// 匹配
            str = m.replaceAll("%26");// 替换所有的&为"%26"

        }
        ;
        return str;
    }

    /**
     * 判断是否null或者空字符 “”
     *
     * @param string
     */
    public static boolean isNull(String string) {
        boolean isNull = true;
        if (string != null && !"".equals(string) && !"null".equals(string) && !"无".equals(string)) {
            isNull = false;
        }
        return isNull;
    }

    /**
     * 判断是否null或者空字符 “”
     *
     * @param string
     */
    public static String isNullRePlace(String string) {
        String str = "";
        if (!"全部".equals(string) && string != null && !"".equals(string) && !"null".equals(string)) {
            return string;
        } else {
            return str;
        }

    }

    // 判断是否特殊符号
    public static boolean StringFilter(String str) {
        // 只允许字母和数字
        // String regEx = "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    // 判断是否特殊符号
    public static boolean StringFilter2(String str) {
        // 只允许字母和数字
        // String regEx = "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx = "[']";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        // return m.replaceAll("").trim();
        return m.find();
    }

    /**
     * 判断是为纯数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {

        Pattern pattern = Pattern.compile("[0-9]*");

        Matcher isNum = pattern.matcher(str);

        if (!isNum.matches()) {

            return false;

        }

        return true;

    }

    // 用于显示提示的
    public static void showToast(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    // 用于显示提示的
    public static void showToast(int messageId, Context context) {
        Toast toast = Toast.makeText(context, messageId, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * 解析血氧设备的序列号
     * @param hexStr
     * @return
     */
    public static String resolveBleMsg(String hexStr) {
        //00010001313530363031363533000000
        //000100304d3730435f30303400000000
        int lenth = 2;
        int i = 4;
        String currentResult = "";
        while (i * lenth < hexStr.length()) {
            String targetStr = "";
            if (i * lenth + lenth > hexStr.length()) {
                targetStr = hexStr.substring(i * lenth, hexStr.length() );
            }else {
                targetStr = hexStr.substring(i * lenth, i * lenth + lenth );
            }

            long hexValue = StringUtil.strtoul(targetStr, 16);
            int value = Integer.valueOf(String.valueOf(hexValue));
            if ((char)value == 0) {
                break;
            }
            currentResult = currentResult.concat((char) value + "");
            i++;
        }

        return  currentResult;
    }


    /**
     * 解析获取到蓝牙传来的16进制数据
     * @param hexStr
     * @return 返回的Peripheral对象中的model为整形的字符串
     */
    public static Peripheral resolveBleMsg_bp(String hexStr) {
        //00010001313530363031363533000000
        int lenth = 2;
        int i = 4;
        String currentResult = "";
        while (i * lenth < hexStr.length()) {
            String targetStr = "";
            if (i * lenth + lenth > hexStr.length()) {
                targetStr = hexStr.substring(i * lenth, hexStr.length() );
            }else {
                targetStr = hexStr.substring(i * lenth, i * lenth + lenth );
            }

            long hexValue = StringUtill.strtoul(targetStr, 16);
            int value = Integer.valueOf(String.valueOf(hexValue));
            if ((char)value == 0) {
                break;
            }
            currentResult = currentResult.concat((char) value + "");
            i++;
        }
        long protocolVersion = StringUtill.strtoul(hexStr.substring(2, 4), 16);
        long deviceModel = StringUtill.strtoul(hexStr.substring(6, 8), 16);

        Peripheral peripheral = new Peripheral();
        peripheral.setModel(String.valueOf(deviceModel));
        peripheral.setPreipheralSN(currentResult);
        peripheral.setProtocolVer(protocolVersion);
        return  peripheral;
    }



}
