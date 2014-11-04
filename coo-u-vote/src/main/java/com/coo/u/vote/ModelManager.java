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
import com.kingstar.ngbf.s.util.PubString;

/**
 * @description
 * @author boqing.shen
 * @date 2014-9-11 下午12:16:31
 * @since 1.0.0.0
 */
public class ModelManager {

	public static List<Channel> TYPE_CHANNNELS = new ArrayList<Channel>();

	static {
		TYPE_CHANNNELS.add(new Channel("yinyue", "音乐"));
		TYPE_CHANNNELS.add(new Channel("tiyu", "体育"));
		TYPE_CHANNNELS.add(new Channel("keji", "科技"));
		TYPE_CHANNNELS.add(new Channel("xinwen", "新闻"));
		TYPE_CHANNNELS.add(new Channel("guoji", "国际"));
		TYPE_CHANNNELS.add(new Channel("minsheng", "民生"));
	}

	/**
	 * 将指定的BasicObject根据@Column注解,生成简单的Map对象 
	 * 用于将对象生成到MongoItem中 进队有注解的字段进行实现
	 * M端发送对象回来,Server端进行转换[方向:M->S]
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
				Object value = "";
				try {
					value = PropertyUtils.getProperty(bo, fieldName);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// 键值...缺省是参照注解,如果没有声明,则按照字段名称来
				String key = col.name();
				if (PubString.isNullOrSpace(key)) {
					key = fieldName;
				}
				item.put(key, value);
			}
		}
		// 放置其它全部属性
//		item.putAll(bo.getAttrs());
		return item;
	}

	/**
	 * 将MI对象的值Merge到BO中[方向:S->M]
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
	 * 将MongoItem对象转换成为Topic对象，传递到M端
	 * [方向:S->M]
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

	private static Long getLong(MongoItem mi, String key) {
		Object value = mi.get(key);
		if (value == null) {
			return 0l;
		}
		return (Long) value;
	}
}
