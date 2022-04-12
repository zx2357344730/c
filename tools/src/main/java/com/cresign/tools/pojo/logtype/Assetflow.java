//package com.cresign.tools.pojo.logtype;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.util.List;
//
//@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
//@AllArgsConstructor//注解在类上，为类提供一个全参的构造方法。
//@Data
//@Document(collection = "Assetflow")
//public class Assetflow {
//
//    public Assetflow(int wn2qtychg, int subtype, String id_to, String id_from,
//                     String id_P, String id_O, String id_C, String ref,
//                     String wcnN, String pic, String zcndesc, Double wn4price,
//                     String wn2qc, List id_L, String grpU, String grpUB) {
//        this.wn2qtychg = wn2qtychg;
//        this.subtype = subtype;
//        this.id_to = id_to;
//        this.id_from = id_from;
//        this.id_P = id_P;
//        this.id_O = id_O;
//        this.id_C = id_C;
//        this.ref = ref;
//        this.wcnN = wcnN;
//        this.pic = pic;
//        this.zcndesc = zcndesc;
//        this.wn4price = wn4price;
//        this.wn2qc = wn2qc;
//        this.id_L = id_L;
//        this.grpU = grpU;
//        this.grpUB = grpUB;
//
//
//    }
//    private static class Hod{
//        private static final Assetflow instance = new Assetflow();    //<clinit>
//    }
//
//    /**
//     * 1、线程安全性保证:线程安全的
//     * 2、性能:好
//     * 3、延迟加载:延迟加载
//     * ##return:  日志
//     */
//    public static Assetflow getInstance(){
//        return Hod.instance;
//    }
//
//
//
//    /**
//     * 索引唯一标识
//     */
//    private String uniqueLog = "assetflow-uniqueLog";
//
//    /**
//     * 数量
//     */
//    private int wn2qtychg;
//    /**
//     * 类型
//     */
//    private int subtype;
//
//    /**
//     * id_A  主
//     */
//    private String id_to;
//
//    /**
//     * id_A  副（要合并去主的id）
//     */
//    private String id_from;
//    /**
//     * 产品id
//     */
//    private String id_P;
//
//    /**
//     * 订单id
//     */
//    private String id_O;
//    /**
//     * 公司id
//     */
//    private String id_C;
//
//    /**
//     * 编号
//     */
//    private String ref;
//
//    /**
//     * 物品名称
//     */
//    private String wcnN;
//    /**
//     * 图片
//     */
//    private String pic;
//    /**
//     * 描述
//     */
//    private String zcndesc;
//    /**
//     * 价格
//     */
//    private Double wn4price;
//    /**
//     * 质量
//     */
//    private String wn2qc;
//    /**
//     * 库存位置
//     */
//    private List id_L;
//    /**
//     * 发的人id
//     */
//    private String grpU;
//    /**
//     * 拿的人id
//     */
//    private String grpUB;
//
//
//
//
//
//
//}
