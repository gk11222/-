package com.imooc.myo2o.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.imooc.myo2o.entity.UserProductMap;

public interface UserProductMapDao {
	List<UserProductMap> queryUserProductMapList(
			@Param("userProductCondition") UserProductMap userProductCondition,
			@Param("rowIndex") int rowIndex, @Param("pageSize") int pageSize);

	int queryUserProductMapCount(
			@Param("userProductCondition") UserProductMap userProductCondition);

	int insertUserProductMap(UserProductMap userProductMap);
}
