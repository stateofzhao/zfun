package com.zfun.learn;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.view.ViewGroup;

/**
 * 对{@link PagerAdapter}的讲解，之前一直没有对PagerAdapter进行深入研究，结果导致了好多小问题。<P>
 *
 * 这个PagerAdapter的经典实现是{@link FragmentStatePagerAdapter}，这里讲一下其中对于数据恢复与保存的实现原理。<Br>
 * 要理解{@link FragmentStatePagerAdapter}对数据保存与恢复的处理，首先需要明确一点，
 * {@link FragmentStatePagerAdapter} 中包含两方面对数据的保存与恢复：<Br>
 * 1.滑动{@link ViewPager}时涉及到的对Fragment状态的保存与恢复。<Br>
 * 2.当{@link ViewPager}销毁时，整个Adapter是如何保存与恢复Adapter所维护的所有Fragment状态的。<Br>
 *
 * 在{@link FragmentStatePagerAdapter}中维护着两个变量 ArrayList<Fragment> mFragments 和
 * ArrayList<Fragment.SavedState> mSavedState ,利用这两个变量实现了上述两方面的数据保存与恢复。<Br>
 *
 * 一、当{@link ViewPager}销毁时，整个Adapter对数据的保存与恢复：<Br>
 *
 * 为什么要使用两个变量来实现数据保存与恢复呢？因为只有PagerAdapter执行了{@link #destroyItem(ViewGroup, int, Object)}
 * 方法时，才会触发保存Fragment的{@link Fragment.SavedState}，才会把Fragment的状态（通过使用{@link
 * FragmentManager#saveFragmentInstanceState(Fragment)}方法来拿到Fragment通过{@link
 * Fragment#onSaveInstanceState(Bundle)} 方法保存的数据）保存到mSavedState中，所以如果在没有执行过
 * {@link #destroyItem(ViewGroup, int, Object)}方法就需要保存数据时（例如，显示了ViewPager但是没有滑动ViewPager），
 * 那么就需要用到mFragments来保存Fragment整个实例到 {@link FragmentManager}中（调用{@link
 * FragmentManager#putFragment(Bundle, String, Fragment)}）
 * 来实现Fragment脱离Adapter存在的方式来恢复数据了。<Br>
 *
 * 需要注意两点：<Br>
 * 1.mFragments中只会保存已经创建出来的页面，被{@link #destroyItem(ViewGroup, int, Object)}过的页面会从
 * mFragments中移除，所以mFragments中最多保存有 通过{@link ViewPager#setOffscreenPageLimit(int)}设置的
 * 数量的Fragment。<Br>
 *
 * 2.mSavedState会保存所有经过{@link #destroyItem(ViewGroup, int, Object)}处理过Fragment的{@link
 * Fragment.SavedState}数据。<Br>
 *
 * 二、滑动{@link ViewPager}时，对Fragment的数据保存与恢复：<Br>
 * 在{@link #destroyItem(ViewGroup, int, Object)}方法时会把销毁的Fragment状态存储到mSavedState中，同时设置
 * mFragments在此position上为空（释放掉对此Fragment的引用，因为此Fragment的状态已经保存到了mSavedState中）。
 * 在{@link #instantiateItem(ViewGroup, int)}方法中，首先检测mFragments中是否保存了此position对应的Fragment，
 * 如果有，直接返回，如果没有就生成新的Fragment 然后把mSavedState中保存的对应的状态设置给Fragment（通过调用
 * {@link Fragment#setInitialSavedState(Fragment.SavedState)}），并且保存Fragment到mFragments变量中。
 *
 * <P>
 *
 * Created by Diagrams on 2017/1/5 10:53
 */
public class LearnPagerAdapter extends PagerAdapter {

  //很好理解，一旦调用，说明ViewPager的内容即将开始改变，紧接着就会调用 {@link #instantiateItem(ViewGroup,int)}
  // 或者{@link #destroyItem(ViewGroup,int,Object)}，然后调用{@link #finishUpdate(ViewGroup)}方法，完成此次刷新。
  @Override public void startUpdate(ViewGroup container) {
    super.startUpdate(container);
  }

  //在这个方法中创建出一个页面，然后将此页面的视图添加到跟定的容器中（第一个参数container），
  // 这个添加过程必须要确保在{@link #finishUpdate(ViewGroup)}方法返回之前执行完毕。
  //@return 返回值一定要是一个与此页面对应的唯一KEY，会把这个返回值传递给
  // {@link #isViewFromObject(View,Object)}第二个参数、{@link #setPrimaryItem(ViewGroup,int,Object)}第三个参数和
  // {@link #destroyItem(ViewGroup,int,Object)}第三个参数。
  @Override public Object instantiateItem(ViewGroup container, int position) {
    return super.instantiateItem(container, position);
  }

  //在给定的位置移除一个页面，需要在这个方法中把页面对应的View从给定的容器中（第一个参数container）移除，
  // 这个移除过程必须确保在{@link #finishUpdate(ViewGroup)}方法返回之前执行完毕。
  //@params object 这个参数就是{@link #instantiateItem(ViewGroup,int)}方法的返回值。
  @Override public void destroyItem(ViewGroup container, int position, Object object) {
    super.destroyItem(container, position, object);
  }

  //通知Adapter哪个页面当前正在显示给用户，一般就是处理当前显示的页面需要显示Toolbar，
  // 非当前显示的页面需要隐藏Toolbar等相关事宜，通过第三个参数来查询出对应页面然后。。。
  @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
    super.setPrimaryItem(container, position, object);
  }

  //当此方法执行完毕后，在instantiateItem(ViewGroup, int)方法返回的key（就是返回值）相对应的视图将会被加入到父ViewGroup中，
  // 而与传递给destroyItem(ViewGroup, int, Object)方法的key相对应的视图将会被移除。
  //一定注意，这个方法返回后就不要再对视图做任何调整了！
  @Override public void finishUpdate(ViewGroup container) {
    super.finishUpdate(container);
  }

  //这个方法之前有过诸多误解，误解为只要判断object==view就ok了，其实不然！
  //用来确定添加的页面的视图是否与{@link #instantiateItem(ViewGroup,int)}方法的返回值（页面View对应的唯一KEY）相对应！
  // 通俗的说就是用来确保页面的视图不出现错位的问题，用来确保给定的View确实是与页面相关联的！
  //此方法是PagerAdapter能够正常工作所必需的！
  @Override public boolean isViewFromObject(View view, Object object) {
    return false;
  }

  //这个方法是相当有用的，当ViewPager执行自己的onSaveInstanceState()方法时（Android系统会在合适的时机自动调用），
  // 会调用此方法来让Adapter保存数据。
  //这个方法会在{@link #destroyItem(ViewGroup,int,Object)}方法之后执行。
  //@return 返回值会保存到Activity/Fragment的saveInstanceBundle中。
  @Override public Parcelable saveState() {
    return super.saveState();
  }

  //这个是恢复数据，会在instantiateItem(ViewGroup, int)方法之前调用。此方法是Android系统通过ViewPager调用的。
  //@params state 这个就是 saveState()方法的返回值
  @Override public void restoreState(Parcelable state, ClassLoader loader) {
    super.restoreState(state, loader);
  }

  @Override public int getCount() {
    return 0;
  }

  @Override public CharSequence getPageTitle(int position) {
    return super.getPageTitle(position);
  }
}
