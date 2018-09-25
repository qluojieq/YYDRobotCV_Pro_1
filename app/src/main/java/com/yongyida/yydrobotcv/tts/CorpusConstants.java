package com.yongyida.yydrobotcv.tts;
import java.util.Random;

public class CorpusConstants {

    public static final String SIR = "sir";
    public static  final String MADAM = "madam";
    public static final String NO_PERSON = "noPerson";
    public static final String VIP_SIR = "vipSir";
    public static final String VIP_MADAM = "vipMadam";




    public static String[] SirSays = new String[]{
            "小哥哥，世界这么大，遇见了你才明白缘分的奇妙", //萌
            "哥哥，哥哥，你真帅" // 萌
    };

    public static String[] MadamSays = new String[]{
            "小姐姐，你真好看",
            "姐姐，姐姐，我是可爱的机器人，你是可爱",
            "姐姐，这是我的手背，这是我的手心，你是我的宝贝",
            "对面的女孩看过来，看过来，看过来，小勇的表演很精采，请不要假装不理不采" // 唱歌
    };

    public static String[] NoMan = new String[]{
            "莫文蔚的阴天，孙燕姿的雨天，周杰伦的晴天，都不如你和我聊天", // 微笑
            "我把万千技能装进身体，只为‘灵’气满满的等候你",
            "我是小勇，很高兴为您服务！"
    };
    public static String[] VipMan = new String[]{
            "先生，我是小勇，很高兴为您服务！"
    };
    public static String[] VipWoman = new String[]{
            "女士，我是小勇，很高兴为您服务！"
    };


    // 不同人说不同的话
    public static String SayHelloWords(String whoType, String name) {

        int index;
        Random random = new Random();
        String ret = "";
        switch (whoType) {
            case SIR:
                index = random.nextInt(SirSays.length);
                ret = SirSays[index];
                break;
            case MADAM:
                index = random.nextInt(MadamSays.length);
                ret = MadamSays[index];
                break;
            case NO_PERSON:
                index = random.nextInt(NoMan.length);
                ret = NoMan[index];
                break;
            case VIP_SIR:
                index = random.nextInt(VipMan.length);
                ret = name + VipMan[index];
                break;
            case VIP_MADAM:
                index = random.nextInt(VipWoman.length);
                ret = name + VipWoman[index];
                break;
        }
        return ret;
    }

}
