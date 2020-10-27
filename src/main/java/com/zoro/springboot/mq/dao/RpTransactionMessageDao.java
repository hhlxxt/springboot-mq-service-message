package com.zoro.springboot.mq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zoro.springboot.mq.entity.RpTransactionMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author zoro
 * @version 1.0
 * @date 2020/10/25 10:48
 * @desc
 */
@Mapper
public interface RpTransactionMessageDao  extends BaseMapper<RpTransactionMessage> {

    /**
     * 根据时间、状态、是否死亡(可选) 查询消息信息
     *
     * @param editTime
     * @param status
     * @param areadlyDead
     * @return
     */
    @Select("<script> SELECT * FROM `transactionmessage` t WHERE  t.`status` =#{status} and t.`edit_time` &lt;= #{editTime}" +
            " <if test=\"areadlyDead != null and areadlyDead != ''\" > " +
            "  and t.`areadly_dead` =#{areadlyDead}" +
            " </if>" +
            " limit 0,100 " +
            "</script>")
    public List<RpTransactionMessage> customSelectPage(String editTime,String status ,String areadlyDead );


}
