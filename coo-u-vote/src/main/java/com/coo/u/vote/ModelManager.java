package com.coo.u.vote;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import com.coo.s.vote.model.BasicObject;
import com.coo.s.vote.model.Channel;
import com.coo.s.vote.model.Column;
import com.coo.s.vote.model.Topic;
import com.coo.s.vote.model.TopicLeg;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.util.GenericsUtil;

/**
 * @description
 * @author boqing.shen
 * @date 2014-9-11 下午12:16:31
 * @since 1.0.0.0
 */

public class ModelManager {

	private static List<Channel> TYPE_CHANNNELS = new ArrayList<Channel>();

	static {
		TYPE_CHANNNELS.add(new Channel("a658cc29-d1ff-4ab7-a879-1a31a6aef169",
				"yinyue", "音乐"));
		TYPE_CHANNNELS.add(new Channel("a658cc29-d1ff-4ab7-a879-1a31a6aef170",
				"tiyu", "体育"));
		TYPE_CHANNNELS.add(new Channel("a658cc29-d1ff-4ab7-a879-1a31a6aef171",
				"worldcup_2014", "2014世界杯"));
		TYPE_CHANNNELS.add(new Channel("a658cc29-d1ff-4ab7-a879-1a31a6aef172",
				"keji", "科技"));
		TYPE_CHANNNELS.add(new Channel("a658cc29-d1ff-4ab7-a879-1a31a6aef173",
				"xinwen", "新闻"));
		TYPE_CHANNNELS.add(new Channel("a658cc29-d1ff-4ab7-a879-1a31a6aef174",
				"guoji", "国际"));
		TYPE_CHANNNELS.add(new Channel("a658cc29-d1ff-4ab7-a879-1a31a6aef175",
				"minsheng", "民生"));
	}

	/**
	 * 将指定的BasicObject根据@Column注解,生成简单的Map对象 用于将对象生成到MongoItem中 进队有注解的字段进行实现
	 */
	public static Map<String, Object> toMap(BasicObject bo) {
		Map<String, Object> item = new HashMap<String, Object>();
		// 获得所有的Field列表，遍历循环
		List<Field> fields = GenericsUtil.getClassSimpleFields(bo.getClass(),
				true);
		for (Field field : fields) {
			Column col = field.getAnnotation(Column.class);
			if (col != null) {
				String fieldName = field.getName();
				System.out.println(fieldName);
				Object value = "";
				try {
					value = PropertyUtils.getProperty(bo, fieldName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				item.put(col.name(), value);
			}
		}
		// 放置其它全部属性
		item.putAll(bo.getAttrs());
		return item;
	}

	/**
	 * 将MI对象的值Merge到BO中
	 */
	public static void merge(MongoItem mi, BasicObject bo) {
		// 获得所有的Field列表，遍历循环
		List<Field> fields = GenericsUtil.getClassSimpleFields(bo.getClass(),
				true);
		for (Field field : fields) {
			Column col = field.getAnnotation(Column.class);
			if (col != null) {
				try {
					// Type type = field.getGenericType();
					// 根据注解的名称,获得数据库的Key
					Object value = mi.get(col.name());
					PropertyUtils.setProperty(bo, field.getName(), value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// 处理专有的框架的字段
		bo.set_id(mi.get_id());
		bo.set_tsu(getLong(mi, "_tsu"));
		bo.set_tsi(getLong(mi, "_tsi"));
	}

	/**
	 * 将MongoItem对象转换成为Topic对象
	 * 
	 * @param mi
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Topic mi2Topic(MongoItem mi) {
		Topic topic = new Topic();
		ModelManager.merge(mi, topic);

		// 获得所有的子Map属性
		List<Map<String, Object>> legs = (List<Map<String, Object>>) mi
				.get("legs");
		for (Map<String, Object> map : legs) {
			// Map转换成为TopicLeg对象
			// TODO 排序...
			TopicLeg tl = new TopicLeg();
			tl.setSeq((String) map.get("leg_seq"));
			tl.setTitle((String) map.get("leg_title"));
			tl.setVote((Integer) map.get("leg_vote"));
			topic.add(tl);
		}
		return topic;
	}

	/**
	 * 根据远端的由Topic生成的Json数据转换而成... 参见ModelManager.mi2Topic
	 */
	public static Map<String, Object> topic2MI(Topic topic) {
		Map<String, Object> item = new HashMap<String, Object>();
		item.put("title", topic.getTitle());
		item.put("owner", topic.getOwner());
		// 設置到期時間
		item.put("expired", 0l);
		item.put("status", Topic.STATUS_VALID);
		// 最新投票數:0
		item.put("vote", 0);
		// 创建快照时间...
		item.put("snapshot", System.currentTimeMillis());
		// 定义所属频道...
		item.put("channels", "");

		List<Map<String, Object>> legs = new ArrayList<Map<String, Object>>();
		for (TopicLeg leg : topic.getLegs()) {
			Map<String, Object> lm = new HashMap<String, Object>();
			lm.put("leg_seq", leg.getSeq());
			lm.put("leg_vote", leg.getVote());
			lm.put("leg_title", leg.getTitle());
			legs.add(lm);
		}
		item.put("legs", legs);
		return item;
	}

	public static Long getLong(MongoItem mi, String key) {
		Object value = mi.get(key);
		if (value == null) {
			return 0l;
		}
		return (Long) value;
	}

	public static Integer getInteger(MongoItem mi, String key) {
		Object value = mi.get(key);
		if (value == null) {
			return 0;
		}
		return (Integer) value;
	}

	public static String getString(MongoItem mi, String key) {
		Object value = mi.get(key);
		if (value == null) {
			return "";
		}
		return (String) value;
	}

	/**
	 * 返回所有的静态的类型频道 u-topic/u-robort等都用到
	 */
	public static List<Channel> getTypeChannels() {
		return TYPE_CHANNNELS;
	}
}
