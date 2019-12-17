package com.atguigu.gmall.search;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallSearchServiceApplicationTests {

	@Autowired
	JestClient jestClient;

	@Reference
	SkuService skuService;



	@Test
	public void contextLoads() throws IOException {
       List<PmsSkuInfo> pmsSkuInfos=skuService.getSkuForSearch();
		//PUT增
		List<PmsSearchSkuInfo> pmsSearchSkuInfos=new ArrayList<>();

		for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
			PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
			BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
			pmsSearchSkuInfo.setId(Long.parseLong(pmsSkuInfo.getId()));
			pmsSearchSkuInfos.add(pmsSearchSkuInfo);
		}

		for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
			Index index = new Index.Builder(pmsSearchSkuInfo).index("gmall0722").type("PmsSearchSkuInfo").id(pmsSearchSkuInfo.getId() + "").build();
    jestClient.execute(index);
		}
	}



	@Test
	public void query()throws Exception{
		//GET 查
		String query=getMyQuery();
		Search search = new Search.Builder(query).addIndex("movie_index").addType("movie").build();

		SearchResult execute = jestClient.execute(search);
		List<SearchResult.Hit<Movie, Void>> hits = execute.getHits(Movie.class);
		for (SearchResult.Hit<Movie, Void> hit : hits) {
			Movie source = hit.source;
			String sourceName = source.getName();
			//String sourceId = source.getId();
			System.out.println(sourceName);
			//System.out.println(sourceId);

		}
	}

	



	public String getMyQuery(){
		// 封装query的dsl
	 SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		String[] actorids=new String[]{"1","2","3"};
		TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("actorList.id",actorids);
		boolQueryBuilder.filter(termsQueryBuilder);
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("name","行动");
		boolQueryBuilder.must(matchQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);

		searchSourceBuilder.from(0);
		searchSourceBuilder.size(20);

		return searchSourceBuilder.toString();
	}

}
