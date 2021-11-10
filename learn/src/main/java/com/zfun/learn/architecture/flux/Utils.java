package com.zfun.learn.architecture.flux;

/**
 * Created by Diagrams on 2016/8/15 19:19
 */
class Utils {
  /**
   * A class that can supply objects of a single type.  Semantically, this could
   * be a factory, generator, builder, closure, or something else entirely. No
   * guarantees are implied by this interface.
   *
   * @author Harry Heymann
   * @since 2.0 (imported from Google Collections Library)
   */
  interface Supplier<T> {
    /**
     * Retrieves an instance of the appropriate type. The returned object may or
     * may not be a new instance, depending on the implementation.
     *
     * @return an instance of the appropriate type
     */
    T get();
  }//class end

  static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }
}
