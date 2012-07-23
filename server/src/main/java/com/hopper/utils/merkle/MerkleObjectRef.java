package com.hopper.utils.merkle;

import com.hopper.session.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-7-20
 * Time: 下午1:31
 * To change this template use File | Settings | File Templates.
 */
public interface MerkleObjectRef extends Serializer {
    String getKey();

    long getVersion();
}
