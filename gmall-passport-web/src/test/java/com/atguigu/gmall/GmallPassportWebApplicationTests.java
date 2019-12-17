package com.atguigu.gmall;


import com.atguigu.gmall.util.HttpclientUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {


	@Test
	public void contextLoads() {

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("client_id","2067366399");
		paramMap.put("client_secret","eeda2ec361060a0141d81a31b15be696");
		paramMap.put("redirect_uri","http://passport.gmall.com:8086/vlogin");
		paramMap.put("grant_type","authorization_code");
		paramMap.put("code","85d43287628ea4eae5fd95482597e9b9");
		String result = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token?", paramMap);

		System.out.println(result);
	}


}
