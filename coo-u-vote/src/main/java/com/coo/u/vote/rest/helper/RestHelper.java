package com.coo.u.vote.rest.helper;

import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;

public class RestHelper {

	/**
	 * TODO STP更新
	 * 
	 * @param errorMsg
	 * @return
	 */
	public static SimpleMessage<?> error(String errorMsg) {
		return SimpleMessage.blank().head(
				SimpleMessageHead.PARAMETER_ERROR.repMsg(errorMsg));
	}
}
