package org.develnext.jphp.ext.crypto;

import org.develnext.jphp.ext.crypto.classes.WrapCipher;
import org.develnext.jphp.ext.crypto.classes.WrapKey;
import php.runtime.env.CompileScope;
import php.runtime.ext.support.Extension;

import javax.crypto.Cipher;
import java.security.Key;

public class CryptoExtension extends Extension {
    public static final String NS = "php\\crypto";

    @Override
    public Status getStatus() {
        return Status.EXPERIMENTAL;
    }

    @Override
    public String[] getPackageNames() {
        return new String[] { "crypto" };
    }

    @Override
    public void onRegister(CompileScope scope) {
        registerWrapperClass(scope, Key.class, WrapKey.class);
        registerWrapperClass(scope, Cipher.class, WrapCipher.class);
    }
}
