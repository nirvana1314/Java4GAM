package gam;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;




public class Snippet {
	private static JSONObject zbObj;
	private static OkHttpClient client;
	private static int listReqCount = 0;
	private static BufferedImage vetifyImage;
	private static int vetifyCode = -1;
	private static String deptID = "41040";// 脾胃病科 41040
	private static String cookie = "acw_tc=AQAAAKfuzl7ziggAS+X5crtpKtRZO3fa";
	
	
	/*****		专家		*****/
//	private static String expertID = "36480";// 测试(陶夏平)
//	private static String expertID = "36484";// 测试(张润顺)
	private static String expertID = "36473";// 周斌 36473
	/*****		患者		*****/
	private static String patientsId = "1066925";// 1066925-王
//	private static String patientsId = "1030058";// 1030058-李
	/*****		日期		*****/
	private static String regDate = "2017-03-13";//	周一(周五预约) 周二(周六预约) 周五(周二预约)
	
	
	public static boolean isWhite(int colorInt) {
		
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() == 765) {
            return true;
        }
        return false;
    }

    public static Integer similar(Map<Integer, Integer> map,int key) {
        for (Map.Entry<Integer,Integer> entry:map.entrySet()) {
            if (cmpColor(entry.getValue(),key)<10)return entry.getKey();
        }
        return -1;
    }

    public static BufferedImage removeBackgroud(BufferedImage img)
            throws Exception {
        int width = img.getWidth();
        int height = img.getHeight();

        
        
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (isWhite(img.getRGB(x,y))) {
                    img.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    img.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        return img;
    }
    
    public static BufferedImage removeColorBackground(BufferedImage img) throws Exception {
        int width = img.getWidth();
        int height = img.getHeight();

		int colorMax = img.getRGB(1, 1);
//		System.out.println("colorMaxcolorMaxcolorMaxcolorMax"+colorMax);
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (cmpColor(img.getRGB(x, y) , colorMax) < 10) {
					img.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
        return img;
    }

    public static int cmpColor(int src,int tar) {
        Color s = new Color(src);
        Color t = new Color(tar);
        int r = Math.abs(s.getRed()-t.getRed());
        int g = Math.abs(s.getGreen()-t.getGreen());
        int b = Math.abs(s.getBlue()-t.getBlue());
        return r+g+b;
    }

    public static BufferedImage removeBlank(BufferedImage img) throws Exception {
        int width = img.getWidth();
        int height = img.getHeight();

        //纵向扫描
        int left = width/2-1;
        int right = width/2;
        for (int i=0;i<14;i++){
            int leftCount = 0;
            int rightCount = 0;
            for (int y = 0; y < height; ++y) {
                if (!isWhite(img.getRGB(left,y))) {
                    leftCount++;
                }
            }
            for (int y = 0; y < height; ++y) {
                if (!isWhite(img.getRGB(right,y))) {
                    rightCount++;
                }
            }
            if (leftCount<rightCount&&right!=width-1||left==0) {
                right++;
            } else {
                left--;
            }
        }

        //横向扫描
        int start = height/2-1;
        int end = height/2;
        for (int i=0;i<20;i++){
            int headCount = 0;
            int footCount = 0;
            for (int x = 0; x < width; ++x) {
                if (!isWhite(img.getRGB(x,start))) {
                    headCount++;
                }
            }
            for (int x = 0; x < width; ++x) {
                if (!isWhite(img.getRGB(x,end))) {
                    footCount++;
                }
            }
            if (headCount<footCount&&end!=height-1||start==0) {
                end++;
            } else {
                start--;
            }
        }
        return img.getSubimage(left, start, right - left +1, end - start + 1);
    }

    public static List<BufferedImage> splitImage(BufferedImage img)
            throws Exception {
        List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
        BufferedImage img1 = img.getSubimage(10, 0, 20, 39);
        img1 = removeBlank(img1);
        subImgs.add(img1);
        BufferedImage img2 = img.getSubimage(30, 0, 20, 39);
        img2 = removeBlank(img2);
        subImgs.add(img2);
        BufferedImage img3 = img.getSubimage(50, 0, 20, 39);
        img3 = removeBlank(img3);
        subImgs.add(img3);
        return subImgs;
    }
    
    

    public static Map<BufferedImage, String> loadTrainData() throws Exception {
        Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
        
//        URL url = Snippet.class.getResource("/train");
        
//        String path = new File(Snippet.class.getResource("/train").getFile()).getAbsolutePath(); 
//        System.out.println(path);
//        File dir = new File(path);

        
//      	String path = "/Users/Will/Downloads/5184CAPTCHA-master/train3";	//	eclipse调试打开
        String path = System.getProperty("user.dir")+"/train";			//	jar包打开
        
        
//        System.out.println(path);
        File dir = new File(path);
        
        File[] files = dir.listFiles();

        for (File file : files) {
        	if (file.getName().equals(".DS_Store")) {
				continue;
			}
        	String keyString = file.getName().charAt(0) + "";
            map.put(ImageIO.read(file), keyString);
        }
        return map;
    }
    
    public static void inputstreamtofile(InputStream ins,File file) throws IOException{
    	OutputStream os = new FileOutputStream(file);
    	int bytesRead = 0;
    	byte[] buffer = new byte[8192];
    	while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
    	os.write(buffer, 0, bytesRead);
    	}
    	os.close();
    	ins.close();
    }

    public static String getSingleCharOcr(BufferedImage img,
                                          Map<BufferedImage, String> map) {
        String result = "";
        int width = img.getWidth();
        int height = img.getHeight();
        int min = width * height;
        for (Map.Entry<BufferedImage, String> entry : map.entrySet()) {
            int count = 0;
            if (entry == null) {
				continue;
			}
            Label1: for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
//                	System.out.println("getSingleCharOcr1"+img.getRGB(x, y));
//                	System.out.println("getSingleCharOcr2"+entry);
                	
                    if (isWhite(img.getRGB(x, y)) != isWhite(entry.getKey().getRGB(x, y))) {
                        count++;
                        if (count >= min)
                            break Label1;
                    }
                }
            }
            if (count < min) {
                min = count;
                result = entry.getValue();
            }
        }
        return result;
    }

    public static BufferedImage removeGanRaoLine(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();
        for (int x = 1; x < width-1; ++x) {
            for (int y = 1; y < height-1; ++y) {
                if (isWhite(img.getRGB(x, y)))continue;
                int center = img.getRGB(x,y);
                int l = img.getRGB(x-1,y);
                int r = img.getRGB(x+1,y);
                int u = img.getRGB(x,y-1);
                int d = img.getRGB(x,y+1);
                if ((!isWhite(l)&&l==center)||(!isWhite(r)&&r==center)||(!isWhite(u)&&u==center)||(!isWhite(d)&&d==center)){

                } else if (isWhite(l)||isWhite(r)||isWhite(u)||isWhite(d)) {
                    img.setRGB(x,y,Color.WHITE.getRGB());
                }
            }
        }
        
        return img.getSubimage(1,1,width-1,height-1);
    }
    
    
    public static String getAllOcr() throws Exception {
        BufferedImage img = Snippet.vetifyImage;
        img = removeColorBackground(img);
        img = removeGanRaoLine(img);//去掉干扰线
        img = removeBackgroud(img);//黑白化
        
        List<BufferedImage> listImg	 = splitImage(img);//纵横向扫描
        Map<BufferedImage, String> map = loadTrainData();
        String result = "";
        for (BufferedImage bi : listImg) {
            String singleChar = getSingleCharOcr(bi, map);
            result += singleChar;
        }
        return result;
    }
    

    public static String getSingleCharNum(String singleChar){
        int c = 1;
        while (new File("fen10/"+singleChar+'-'+(++c)).exists()||new File("temp/"+singleChar+'-'+(c)).exists());
        return singleChar+'-'+c;
    }
    
    public static void trainData() throws Exception {  
    	File dir1 = new File("/Users/Will/Downloads/5184CAPTCHA-master/img/");  
        File[] files1 = dir1.listFiles(); 
        int index = 0;
		for (File file : files1) {
			if (file.getName().equals(".DS_Store")) {
				continue;
			}
			System.out.println(file.getName());
        	BufferedImage img = ImageIO.read(file);
            img = removeColorBackground(img);
            img = removeGanRaoLine(img);
            img = removeBackgroud(img);
            List<BufferedImage> listImg = splitImage(img);
            if (listImg.size() == 3) {  
                for (int j = 0; j < listImg.size(); ++j) {  
                    ImageIO.write(listImg.get(j), "PNG", new File("/Users/Will/Downloads/5184CAPTCHA-master/train3/"  
                            + file.getName().charAt(j) + "^" + (index++)  
                            + ".png"));  
                }  
            } 
		}
    	 
    } 
    
    public static boolean isNumeric(String str){
    	  for (int i = 0; i < str.length(); i++){
    	   if (!Character.isDigit(str.charAt(i))){
    	    return false;
    	   }
    	  }
    	  return true;
    	 }
    
    public static void getResult() throws Exception {
    	long start = System.currentTimeMillis();

      	String text = getAllOcr();  
      	String BeginStr = text.substring(0, 1);
      	String calCode = text.substring(1, 2);
      	String endStr = text.substring(2, 3);
      	int result = 0;
      	if (isNumeric(BeginStr) && isNumeric(endStr)) {
      		int BeginNum = Integer.parseInt(BeginStr);
          	int endNum = Integer.parseInt(endStr);
          	if (calCode.equals("+")) {
  				result = BeginNum + endNum;
  			}else if (calCode.endsWith("-")) {
  				result = BeginNum - endNum;
  			}else if (calCode.endsWith("*")) {
  				result = BeginNum * endNum;
  			}else {
  				// 异常
  				System.out.println("不是运算符");
  				getVetifyCode();
  				return;
  			}
          	
          	if (result < 0 || result > 9) {
          		// 异常
          		System.out.println("结果越界");
          		getVetifyCode();
          		return;
          	}
          	
      	}else {
				// 异常 不是数字
				System.out.println("异常, 不是数字" + ".png"+ "BeginStr" + BeginStr + "endStr" + endStr);
				getVetifyCode();
				return;
      	}
      	
      	System.out.println("解析成功!!!" + "\n解析值:" + text + " 运算结果为:" + result);
      	System.out.println(result);
      	vetifyCode = result;

        System.out.println(System.currentTimeMillis()-start+"ms");
        //	挂号申请
      	doApply();
    }
    
    public static void doApply() throws Exception {
    	System.out.println("doApplyStart"+ new Date());
    	if (vetifyCode == -1) {
    		System.out.println("验证码尚未请求"+vetifyCode);
    		return;
		}else if (zbObj == null) {
			System.out.println("排班id尚未获取");
    		return;
		}
    	
    	String urlStr = "http://app.zhicall.cn/mobile-web/mobile/guahao/apply/new";
    	RequestBody body = new FormBody.Builder()  
    	//	静态参数
        .add("deviceId", "6725759D-6284-43C4-95C6-90CD043F751D")//添加键值对  
        .add("version", "1.2.2")  
        .add("hospitalId", "10097")  
        .add("accountId", "153657")  
        .add("patientsId", patientsId)	  
        .add("medicalCardId", "-1")  
        .add("deptId", deptID)  
        .add("expertId", expertID)  
        //	动态参数
        .add("scheduleId", zbObj.get("scheduleId")+"")  
        .add("price", zbObj.get("price")+"")  
        .add("regDate", zbObj.get("regDate")+"")  
        .add("regTime", zbObj.get("regTime")+"")  
        .add("verifyCode", vetifyCode+"")  
        .build();  
    	Request request = new Request.Builder()  
        .url(urlStr)  
        .post(body)  
        .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 8_4 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Mobile/12H143 (384053392)ZhiCall/5316")
        .addHeader("Cookie", cookie)
        .build();  
    	
    	Response response = client.newCall(request).execute(); 
    	
    	if(response.isSuccessful())  {
    		String str = response.body().string();
    		System.out.println(str);
    		JSONObject obj = JSONObject.fromObject(str);
            String errMsg = (String) obj.get("errMsg");
            if (errMsg.equals("无效的验证码")) {
				System.out.println("异常 验证码不对 重新请求验证码");
				getVetifyCode();
			}
    	}else {
			// 请求失败
		}
    }
    
    public static void getVetifyCode() throws Exception{
//    	System.out.println("getVetifyCodeStart");
    	String urlStr = "http://app.zhicall.cn/mobile-web/mobile/barCode/153657/verify";
    	Request request = new Request.Builder()  
        .url(urlStr)   
        .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 8_4 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Mobile/12H143 (384053392)ZhiCall/5316")
        .addHeader("Cookie", cookie)
        .build();
    	Response response;
		try {
			response = client.newCall(request).execute();
			if(response.isSuccessful())  {
//	    		System.out.println("验证码请求成功");
				// 获取图片 
	    		InputStream is = new ByteArrayInputStream(response.body().bytes());
		        vetifyImage = ImageIO.read(is);
		        getResult();
	    	}else {
				// 请求失败
//	    		System.out.println("验证码请求失败");
			}
		} catch (IOException e) {
//			System.out.println("getVetifyCodeCatch!!!");
			e.printStackTrace();
			getVetifyCode();
		} 
    	
    	
    	
    }
    
    
    public static void doRequestList() throws Exception {
    	if (zbObj != null) {
//    		System.out.println("obj已存在!!!");
			return;
		}
    	String urlStr = "http://app.zhicall.cn/mobile-web/mobile/schedule/hospital/10097/dept/"+deptID+"/oneDay";
    	System.out.println("doRequestListStart");
        //3, 发起新的请求,获取返回信息  
        RequestBody body = new FormBody.Builder()  
                            .add("deviceId", "6725759D-6284-43C4-95C6-90CD043F751D")//添加键值对  
                            .add("version", "1.2.2")  
                            .add("hospitalId", "10097")  
                            .add("regDate", regDate)  
                            .build();  
        Request request = new Request.Builder()  
        					.tag("list")
                            .url(urlStr)  
                            .post(body)  
                            .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 8_4 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Mobile/12H143 (384053392)ZhiCall/5316")
                            .addHeader("Cookie", cookie)
                            .build();  
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
			
			public void onResponse(Call arg0, Response response) throws IOException {
				System.out.println("list-onResponse"+ new Date());
				// TODO Auto-generated method stub
				if(response.isSuccessful())  {  
		            String str = response.body().string();  
		            JSONObject obj = JSONObject.fromObject(str);
		            JSONArray data = (JSONArray) obj.get("data");
		            
		            if (data.size() > 0) {
		            	client.dispatcher().cancelAll();
		            	
						for (int i = 0; i < data.size(); i++) {
							JSONObject subObj = data.getJSONObject(i);
//							System.out.println(subObj);
							if (subObj.get("id").toString().equals(expertID)) {
								System.out.println("list请求成功 targetName="+subObj.get("name"));
								JSONArray subArr = (JSONArray) subObj.get("regScheduleVOList");
								zbObj = subArr.getJSONObject(0);
								
//								System.out.println(zbObj);
								try {
									doApply();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							}else {
								continue;
							}
						}
					}else {
						try {
							doRequestList();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						System.out.println("未获取到数据 重试"+count++);						}
					System.out.println("未获取到数据");
					}
		        } else {
					// 请求失败
		        	System.out.println("doRequest请求失败");
				}
			}
			
			public void onFailure(Call arg0, IOException arg1) {
				// TODO Auto-generated method stub
				// 请求失败
	        	System.out.println("doRequestOnFailure"+arg0);
	        	try {
					doRequestList();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
    }
    public static void main(String[] args) throws Exception {
    	client = new OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build();
    	
    	for (String string : args) {
        	System.out.println("out="+string);
        	vetifyCode = Integer.parseInt(string);
        	System.out.println(vetifyCode);
		}
//    	System.out.println("args="+args);
//    	getVetifyCode();
//    	doRequestList();
    	startTimer();
    }
    
    public static void startTimer() {  
    	int hour = 9;
    	int minute = 14;
    	int second = 53;
//    	int hour = 11;
//    	int minute = 29;
//    	int second = 55;
    	System.out.println("开始时间=" + new Date());
    	System.out.println("科室="+deptID+"     -----(脾胃科-41040)\n" + "专家="+expertID+"     -----(周斌-36473, 陶夏平-36480, 张润顺-36484)"+"\nPatientsId="+patientsId+"     -----(王-1066925, 李-1030058)"+"\n预约时间="+regDate+"\n开始抢号时间="+hour+"-"+minute+"-"+second+"\n-waiting...");
        Calendar calendar = Calendar.getInstance();  
        calendar.set(Calendar.HOUR_OF_DAY, hour); 		// 控制时  
        calendar.set(Calendar.MINUTE, minute);       	// 控制分
        calendar.set(Calendar.SECOND, second);       	// 控制秒
  
        Date time = calendar.getTime();         	// 得出执行任务的时间,此处为今天的9:14:55 
  
        Timer timer = new Timer();  
        timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				System.out.println("doTimer!!!\n当前时间=" + new Date());
				try {
					getVetifyCode();
//					TaskList();
					listTimer();
//					doRequestList();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, time, 1000 * 60 * 60 * 24);
        
    }
    
    
    public static void listTimer() {  
    	int hour = 9;
    	int minute = 14;
    	int second = 54;
    	System.out.println("listTimerStart");
    	Calendar calendar = Calendar.getInstance();  
        calendar.set(Calendar.HOUR_OF_DAY, hour); 		// 控制时  
        calendar.set(Calendar.MINUTE, minute);       	// 控制分
        calendar.set(Calendar.SECOND, second);       	// 控制秒
  
        Date time = calendar.getTime();         	// 得出执行任务的时间,此处为今天的9:14:55 
  
        Timer timer = new Timer();  
        timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				System.out.println("doListTimer!!!\n当前时间=" + new Date());
				try {
					TaskList();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, time, 1000 * 60 * 60 * 24);
        
    }
    
    public static void TaskList() {  
    	Runnable runnable = new Runnable() {  
            public void run() {  
            	if (listReqCount > 20) {
					return;
				}
                // task to run goes here  
                System.out.println("TaskListRun - listReqCount="+listReqCount);  
                listReqCount++;
                try {
					doRequestList();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }  
        };  
        ScheduledExecutorService service = Executors  
                .newSingleThreadScheduledExecutor();  
        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间  
        service.scheduleAtFixedRate(runnable, 0, 100, TimeUnit.MILLISECONDS);
    }
    
}
