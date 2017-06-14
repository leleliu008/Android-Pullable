package com.fpliu.newton.ui.pullable;

/**
 * 可以下拉和可以上拉的策略接口 - 策略模式
 *
 * @author 792793182@qq.com
 */
public interface Pullable {

	void canPullDown(boolean canPullDown);

	void canPullUp(boolean canPullUp);

	/**
	 * 判断是否可以下拉，如果不需要下拉功能可以直接return false
	 * 
	 * @return true如果可以下拉否则返回false
	 */
	boolean canPullDown();

	/**
	 * 判断是否可以上拉，如果不需要上拉功能可以直接return false
	 * 
	 * @return true如果可以上拉否则返回false
	 */
	boolean canPullUp();
	
}
