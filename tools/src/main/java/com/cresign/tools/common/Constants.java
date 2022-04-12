package com.cresign.tools.common;

import java.util.Map;

/**
 * ##author: tangzejin
 * ##updated: 2019/12/30
 * ##version: 1.0.0
 * ##description: 通用静态参数使用类
 */
public class Constants {
    public static final String WEB_SOCKET_SERVER_SERVER_ENDPOINT_URL = "/ws/{compID}/{id}";

    public static final String PERSONAL_ID = "u-6ea7d1ac8ff14bea86418a1a495c98a5";

    public static final String WX_APP_ID = "wxb996e85f02688990";

    public static final String WX_MCH_ID = "1590999771";

    public static final String WX_KEY = "ef3dd51c7f0247a7a3abd010569f0f44";

    // 微信统一下单api地址
    public static final String WX_UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    // 微信查询订单api地址
    public static final String WX_QUERY_ORDER_URL = "https://api.mch.weixin.qq.com/pay/orderquery";

    // 微信关闭订单api地址
    public static final String WX_CLOSE_ORDER_URL = "https://api.mch.weixin.qq.com/pay/closeorder";

    /**
     * 请求参数PathParam
     */
    public static final String PATH_PARAM_COMP_ID = "compID";
    public static final String PATH_PARAM_ID = "id";

    /**
     * 布尔类型
     */
    public static final boolean BOOLEAN_FALSE = false;
    public static final boolean BOOLEAN_TRUE = true;

    /**
     *  格式化使用
     */
    public static final String DIGIT_ROUNDING = "#.00";

    /**
     *  定义后缀
     */
    public static final String PDF_SUFFIX = ".pdf";
    public static final String EXCEL_X_SUFFIX = ".xlsx";

    /**
     * 定义时间
     */
    public static final int DATE_SIXTY = 60;
    public static final int DATE_TWENTY_FOUR = 24;
    public static final int DATE_ONE_THOUSAND = 1000;

    /**
     * 定义符号：+
     */
    public static final String PLUS = "+";
    public static final String SPOT = ".";
    public static final String COLON = ":";
    public static final String UNDERLINE = "_";

    /**
     * 定义object
     */
    public static final Object OBJECT_NULL = null;

    /**
     * 定义mapStringObject
     */
    public static final Map<String,Object> MAP_S_O_NULL = null;

    /**
     * 定义字符串
     */
    public static final String _UNDERLINE_ = "_";
    public static final String STRING_LOG_HEADER = "logHeader";
    public static final String STRING_ACTION = "action";
    public static final String STRING_CUSMSG = "cusmsg";
    public static final String STRING_STORAGE = "storage";
    public static final String STRING_QUALITY = "quality";
    public static final String STRING_ZON = "zon";
    public static final String STRING_FALSE = "false";
    public static final String STRING_TRUE = "true";
    public static final String STRING_EMPTY = "";
    public static final String STRING_LOG = "log";
    public static final String STRING_MSG = "msg";
    public static final String STRING_NULL = null;
//    public static final String STRING_TYPE = "type";

    public static final String STRING_PROD = "prod";
    public static final String STRING_PROB = "prob";
    public static final String STRING_C_19 = "c-19";
    public static final String STRING_C_10 = "c-10";
    public static final String STRING_OK = "ok";
    public static final String STRING_BLANK_SPACE = " ";
    public static final String STRING_BMD_LOG_TYPE = "bmdlogType";
    public static final String STRING_ALL = "all";
    public static final String STRING_LB_PROD = "lBProd";
    public static final String STRING_LS_PROD = "lSProd";
    public static final String STRING_LB_ORDER = "lBOrder";
    public static final String STRING_LS_ORDER = "lSOrder";
//    public static final String STRING_INFO = "info";
//    public static final String STRING_PACK = "pack";
//    public static final String STRING_SPEC = "spec";
//    public static final String STRING_V_SPEC = "Vspec";

    /**
     * HashMap默认设置大小
     * 详情参考:https://blog.csdn.net/l18848956739/article/details/85998121
     */
    public static final int HASH_MAP_DEFAULT_LENGTH = 16;

    /**
     * 返回结果
     */
    public static final String RESULT = "result";

    /**
     * 错误
     */
    public static final String ERROR = "error";

    /**
     * 定义常用字符串数字 1
     */
    public static final String STRING_NUMBER_ZERO = "0";
    public static final String STRING_NUMBER_ONE = "1";
    public static final String STRING_NUMBER_TWO = "2";
    public static final String STRING_NUMBER_THERR = "3";
    public static final String STRING_NUMBER_TEN = "10";

    /**
     *
     */
    public static final String STRING_DOUBLE_ZERO = "0.0";

    /**
     * 定义头参数：authorization
     */
    public static final String HEAD_AUTHORIZATION = "authorization";

    /**
     * 定义请求参数
     */
    public static final String REQUEST_ID = "id";
    public static final String REQUEST_YEAR = "year";
    public static final String REQUEST_MONTH = "month";
    public static final String REQUEST_CID = "cId";
    public static final String REQUEST_ID_C = "id_C";
    public static final String REQUEST_OID = "oId";
    public static final String REQUEST_UID = "uId";
    public static final String REQUEST_ID_U = "id_U";
    public static final String REQUEST_PID = "pId";
    public static final String REQUEST_LOG_TYPE = "logType";
    public static final String REQUEST_DATE = "date";
    public static final String REQUEST_PAGE = "page";
    public static final String REQUEST_PAGE_SIZE = "pageSize";
    public static final String REQUEST_TYPE = "type";
    public static final String REQUEST_ERR_ID = "errId";
    public static final String REQUEST_ERR_ID_INDEX = "errIdIndex";
    public static final String REQUEST_O_COMP_ID = "oCompId";
    public static final String REQUEST_INDEX = "index";
    public static final String REQUEST_DESC = "desc";
    public static final String REQUEST_STATUS = "status";
    public static final String REQUEST_SUBSCRIPT = "subscript";
    public static final String REQUEST_PDF_FORMAT = "pdfFormat";
    public static final String REQUEST_DATA_KEY = "dataKey";
    public static final String REQUEST_NAME = "name";

    /**
     * 定义获取值
     */
    public static final String GET_BIS_ORDER = "bisOrder";
    public static final String GET_Z_CN_PREP = "zcnprep";
    public static final String GET_Z_CN_DESC = "zcndesc";
    public static final String GET_PRIORITY = "priority";
    public static final String GET_PRIORITY_S = "prioritys";
    public static final String GET_PAN = "pan";
    public static final String GET_PROD_LIST = "prodList";
    public static final String GET_ORDER_O_ITEM = "orderOItem";
    public static final String GET_ORDER_ACTION = "orderAction";
    public static final String GET_ORDER_PROB = "orderProb";
    public static final String GET_ORDER_FLOW = "orderFlow";
    public static final String GET_ORDER_O_ITEM_LIST = "orderOItemList";
    public static final String GET_ORDER_ACTION_LIST = "orderActionList";
    public static final String GET_ORDER_PROB_LIST = "orderProbList";
    public static final String GET_O_ITEM = "oItem";
    public static final String GET_ACTION = "action";
    public static final String GET_PROB = "prob";
    public static final String GET_ORDER = "order";
    public static final String GET_CUSMSG = "cusmsg";
    public static final String GET_STORAGE = "storage";
    public static final String GET_QUALITY = "quality";
    public static final String GET_ORDER_INFO = "orderInfo";
    public static final String GET_USER_DIST = "userDist";
    public static final String GET_DIST = "dist";
    public static final String GET_LONG = "long";
    public static final String GET_LAT = "lat";
    public static final String GET_OBJ_GRP = "objGrp";
    public static final String GET_PAGING_ROW = "pagingRow";
    public static final String GET_USER_LIST_ID = "userListId";
    public static final String GET_ERROR_BC = "errorBc";
    public static final String GET_LT_D_HO_LI_DAY = "ltdholiday";
    public static final String GET_WCN_N_REAL = "wcnNReal";
    public static final String GET_ROLE_X = "rolex";
    public static final String GET_OBJ_COMP = "objComp";
    public static final String GET_ID_U = "id_U";
    public static final String GET_ID_O = "id_O";
    public static final String GET_U_ID = "uId";
    public static final String GET_KEY_LIST = "keyList";
    public static final String GET_VAL_LIST = "valList";
    public static final String GET_CONTENT = "content";
    public static final String GET_BACKGROUND = "background";
    public static final String GET_FONT_SIZE = "fontSize";
    public static final String GET_IS_FIXED = "isFixed";
    public static final String GET_IS_BOLD = "isBold";
    public static final String GET_OCCUPY_ROW = "occupyRow";
    public static final String GET_DATA_KEY = "dataKey";
    public static final String GET_EXCEL_FORMAT = "excelFormat";
    public static final String GET_PLUS_KEY = "plusKey";
    public static final String GET_PLUS_INDEX = "plusIndex";
    public static final String GET_WIDTHS = "widths";
    public static final String GET_MERGE_TITLE = "mergeTitle";
    public static final String GET_MERGE_FORM = "mergeForm";
    public static final String GET_BC_D_ERROR = "bcderror";
    public static final String GET_TITLE = "title";
    public static final String GET_TITLE_LIST = "titleList";
    public static final String GET_INFO = "info";
    public static final String GET_PACK = "pack";
    public static final String GET_PIC = "pic";
    public static final String GET_L_U_T = "lUT";
    public static final String GET_L_C_R = "lCR";
    public static final String GET_W_N_2_QTY_NEED = "wn2qtyneed";
    public static final String GET_W_N_2_QTY_NEED_S = "wn2qtyneeds";
    public static final String GET_W_N_2_QTY_NOW = "wn2qtynow";
    public static final String GET_IS_WHOLE = "isWhole";
    public static final String GET_IS_FIN = "isFin";
    public static final String GET_WCN_N = "wcnN";
    public static final String GET_WRD_N = "wrdN";
    public static final String GET_WRD_DESC = "wrddesc";
    public static final String GET_WRD_PREP = "wrdprep";
    public static final String GET_CN = "cn";
    public static final String GET_EN = "en";

    public static final String GET_LPT = "lPT";
    public static final String GET_ID = "id";
    public static final String GET_ID_C = "id_C";
    public static final String GET_ID_C_B = "id_CB";
    public static final String GET_ID_C_S = "id_CS";
    public static final String GET_ID_P = "id_P";
    public static final String GET_DESC = "desc";
    public static final String GET_GRP_U = "grpU";
    public static final String GET_GRP_P = "grpP";
    public static final String GET_GRP_B = "grpB";
    public static final String GET_NEXT_DESC = "nextDesc";
    public static final String GET_UPPER_DESC = "upperDesc";
    public static final String GET_WN_0_PRIOR = "wn0prior";
    public static final String GET_WN_4_PRICE = "wn4price";
    public static final String GET_ZN_4_MNY_PRICE = "zn4mnyprice";
    public static final String GET_WN_2_QTY = "wn2qty";
    public static final String GET_WN_4_QTY_NEED = "wn4qtyneed";
    public static final String GET_MULTITASK_COUNT = "multitaskCount";
    public static final String GET_SUBSCRIPT = "subscript";
    public static final String GET_LOG_S = "logs";
    public static final String GET_PROD_ID = "prodId";
    public static final String GET_PROD_ID_S = "prodIds";
    public static final String GET_ERR_ID = "errId";
    public static final String GET_ERR_ID_INDEX = "errIdIndex";
    public static final String GET_NUM = "num";
    public static final String GET_C_ID = "cId";
    public static final String GET_REF = "ref";
    public static final String GET_O_ID = "oID";
    public static final String GET_O_I_D = "oId";
    public static final String GET_O_ID_T = "oIDT";
    public static final String GET_INDEX = "index";
    public static final String GET_INDEX_T = "indexT";
    public static final String GET_INDEX_ONLY = "indexOnly";
//    public static final String GET_ERR_ID_INDEX = "errIdIndex";
//    public static final String GET_VALUE_INDEX_ONLY = "indexOnly";

//    public static final String GET_O_COMP_ID = "oCompId";
    /**
     * 零件描述
     */
    public static final String GET_PRNT_C = "prntC";
    public static final String GET_ID_C_P = "id_CP";
    public static final String GET_IS = "is";
    public static final String GET_COUNT = "count";

    public static final String GET_HR = "hr";
    public static final String GET_EX_HR = "exhr";
    public static final String GET_WK_HR = "wkhr";
    public static final String GET_TYPE = "type";
    public static final String GET_DATE = "date";
    public static final String GET_STATE = "state";
    public static final String GET_STATUS = "status";
    public static final String GET_TMD_CHK = "tmdChk";
    public static final String GET_W_N_0_COME_MIN = "wn0comeMin";
    public static final String GET_W_N_0_LEAVE_MIN = "wn0leaveMin";
    public static final String GET_W_N_0_COME_START = "wn0comeStart";
    public static final String GET_W_N_0_LEAVE_START = "wn0leaveStart";
    public static final String GET_MODE = "mode";
    public static final String GET_W_N_0_COME_LIMIT = "wn0comeLimit";
    public static final String GET_W_N_0_LEAVE_LIMIT = "wn0leaveLimit";
    public static final String GET_STRING_S_1 = "strings1";
    public static final String GET_STRING_S = "strings";
    public static final String GET_CHK_IN_RESULT = "chkInResult";
    public static final String GET_SUM_CHK_IN_X = "sumChkInX";
    public static final String GET_USER_COMP_SUM_CHK_IN = "userCompSumChkIn";
    public static final String GET_COMPANY = "company";
    public static final String GET_L_S_T = "lST";
    public static final String GET_REF_U = "refU";
    public static final String GET_TMD_1 = "tmd1";
    public static final String GET_LATE_DATE = "lateDate";
    public static final String GET_UPPER_CLASS = "upperClass";
    public static final String GET_HOUR_LESS = "hourLess";
    public static final String GET_TMD_2 = "tmd2";
    public static final String GET_LEAVE_EARLY_DATE = "leaveEarlyDate";
    public static final String GET_LOWER_CLASS = "lowerClass";
    public static final String GET_OWN = "own";
    public static final String GET_HOUR = "hour";
    public static final String GET_MIN = "min";
    public static final String GET_SEC = "sec";
    public static final String GET_SHU = "shu";
    public static final String GET_TMD = "tmd";
    public static final String GET_KEY = "key";
    public static final String GET_YUAN = "yuan";
    public static final String GET_BIS_LATE = "bisLate";
    public static final String GET_BIS_DIST = "bisDist";
    public static final String GET_BCD_DIST = "bcdDist";
    public static final String GET_BCD_STATUS = "bcdStatus";
    public static final String GET_BIS_GPS = "bisGPS";
    public static final String GET_ID_C_ID_C = "idC";
    public static final String GET_NAME = "name";
    public static final String GET_ALL_CHK_IN = "allChkIn";
    public static final String GET_WEEK = "week";
    public static final String GET_DATA = "data";
    public static final String GET_TMK = "tmk";
    public static final String GET_CHK_TIME = "chkTime";
    public static final String GET_OBJ_ITEM = "objItem";
    public static final String GET_OBJ_ACTION = "objAction";
    public static final String GET_OBJ_MERGE = "objMerge";
    public static final String GET_OBJ_PROB = "objProb";
    public static final String GET_OBJ_PART = "objPart";
    public static final String GET_OBJ_SPEC = "objSpec";
    public static final String GET_OBJ_FLOW = "objFlow";
    public static final String GET_OBJ_DATA = "objData";
    public static final String GET_OBJ_ORDER = "objOrder";
    public static final String GET_OBJ_CUSMSG = "objCusmsg";
    public static final String GET_OBJ_CUS_USER = "objCusUser";
    public static final String GET_OBJ_STORAGE = "objStorage";
    public static final String GET_OBJ_QUALITY = "objQuality";
    public static final String GET_PDF_TYPE = "pdfType";
    public static final String GET_BM_D_PT = "bmdpt";

    /**
     * 定义添加值
     */
    public static final String ADD_PRIORITY = "priority";
    public static final String ADD_ERR_IND = "errInd";
    public static final String ADD_SCORE = "score";
    public static final String ADD_U_U_ID = "uuId";
    public static final String ADD_TYPE_TEST = "typeTest";
    public static final String ADD_RES_ORDER_ID = "resOrderId";
    public static final String ADD_SCORE_OK = "scoreOk";
    public static final String ADD_SEND_USER = "sendUser";
    public static final String ADD_IS_OK_S = "isOks";
    public static final String ADD_RE = "re";
    public static final String ADD_P_LIST = "pList";
    public static final String ADD_L = "l";
    public static final String ADD_IS_OK = "isOk";
    public static final String ADD_ID_C_B_P = "id_CBP";
    public static final String ADD_WRD_DESC = "wrddesc";
    public static final String ADD_WRD_N = "wrdN";
    public static final String ADD_DEP = "dep";
    public static final String ADD_CN = "cn";
    public static final String ADD_COMP_ID_S = "compIds";
    public static final String ADD_PROD_NUM = "prodNum";
    public static final String ADD_IS_DG = "isDg";
    public static final String ADD_GRP_B_GROUP = "grpBGroup";
    public static final String ADD_Z_CN_PREP = "zcnprep";
    public static final String ADD_Z_CN_DESC = "zcndesc";
    public static final String ADD_MERGE_ID = "mergeId";
    public static final String ADD_O_ID_S = "oIds";
    public static final String ADD_OBJ_DATA = "objData";
    public static final String ADD_SUB_TYPE = "subType";
    public static final String ADD_BIS_ORDER = "bisOrder";
    public static final String ADD_OBJ_PART = "objPart";
    public static final String ADD_OBJ_SPEC = "objSpec";
    public static final String ADD_ORDER = "order";
    public static final String ADD_ORDER_INFO = "orderInfo";
    public static final String ADD_PROD_LIST = "prodList";
    public static final String ADD_ORDER_O_ITEM = "orderOItem";
    public static final String ADD_ORDER_ACTION = "orderAction";
    public static final String ADD_ORDER_PROB = "orderProb";
    public static final String ADD_ORDER_FLOW = "orderFlow";
    public static final String ADD_ORDER_O_ITEM_LIST = "orderOItemList";
    public static final String ADD_ORDER_ACTION_LIST = "orderActionList";
    public static final String ADD_ORDER_PROB_LIST = "orderProbList";
    public static final String ADD_ORDER_CUSMSG = "orderCusmsg";
    public static final String ADD_ORDER_STORAGE = "orderStorage";
    public static final String ADD_ORDER_QUALITY = "orderQuality";
    public static final String ADD_OBJ_ITEM = "objItem";
    public static final String ADD_OBJ_O_ITEM = "objOItem";
    public static final String ADD_OBJ_ACTION = "objAction";
    public static final String ADD_OBJ_PROB = "objProb";
    public static final String ADD_OBJ_FLOW = "objFlow";
    public static final String ADD_OBJ_CUSMSG = "objCusmsg";
    public static final String ADD_OBJ_CUS_USER = "objCusUser";
    public static final String ADD_OBJ_STORAGE = "objStorage";
    public static final String ADD_OBJ_QUALITY = "objQuality";
    public static final String ADD_OBJ_ORDER = "objOrder";
    public static final String ADD_USER_DIST = "userDist";
    public static final String ADD_IS_FIXED = "isFixed";
    public static final String ADD_PAGING_ROW = "pagingRow";
    public static final String ADD_FORM_BODY = "formBody";
    public static final String ADD_OWN = "own";
    public static final String ADD_W_N_0_CNT_USER = "wn0cntUser";
    public static final String ADD_DESC_INFO = "descInfo";
    public static final String ADD_METHOD = "method";
    public static final String ADD_CODE = "code";
    public static final String ADD_SEC = "sec";
    public static final String ADD_MIN = "min";
    public static final String ADD_HOUR = "hour";


    public static final String ADD_ID = "id";

    public static final String ADD_IS = "is";
    public static final String ADD_L_P_T = "lPT";
    public static final String ADD_BM_D_PT = "bmdpt";
    public static final String ADD_DATA = "data";
    public static final String ADD_W_CN_F_N = "wcnFN";
    public static final String ADD_DATE = "date";
    public static final String ADD_DATE_S = "dateS";
    public static final String ADD_ID_U = "id_U";
    public static final String ADD_ID_U_S = "id_Us";
    public static final String ADD_ID_U_PROPOSE = "id_UPropose";
    public static final String ADD_LATE_ALLOW = "lateAllow";
    public static final String ADD_BIS_LATE = "bisLate";
    public static final String ADD_BIS_DIST = "bisDist";
    public static final String ADD_BCD_DIST = "bcdDist";
    public static final String ADD_LATE = "late";
    public static final String ADD_Z_N_1_HOUR_LESS = "zn1hourLess";
    public static final String ADD_W_N_0_CNT_DATE = "wn0cntDate";
    public static final String ADD_IMG_PATH = "imgPath";
    public static final String ADD_URL = "url";
    public static final String ADD_PDF_TYPE = "pdfType";

    /**
     * 定义int数字
     */
    public static final int INT_NEGATIVE_ONE = -1;
    public static final int INT_ZERO = 0;
    public static final int INT_ONE = 1;
    public static final int INT_TWO = 2;
    public static final int INT_THREE = 3;
    public static final int INT_FOUR = 4;
    public static final int INT_FIVE = 5;
    public static final int INT_SIX = 6;
    public static final int INT_SEVEN = 7;
    public static final int INT_EIGHT = 8;
    public static final int INT_NINE = 9;
    public static final int INT_TEN = 10;
    public static final int INT_ELEVEN = 11;
    public static final int INT_TWELVE = 12;
    public static final int INT_THIRTEEN = 13;
    public static final int INT_FOURTEEN = 14;
    public static final int INT_FIFTEEN = 15;
    public static final int INT_EIGHTEEN = 18;
    public static final int INT_TWENTY = 20;
    public static final int INT_TWENTY_ONE = 21;
    public static final int INT_TWENTY_SIX = 26;
    public static final int INT_THIRTY = 30;
    public static final int INT_SIXTY = 60;
    public static final int INT_TWO_HUNDRED_AND_FIFTY = 250;

}
