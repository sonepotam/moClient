/**
 * $RCSfile$
 * version $Revision: 38168 $
 * created 15.08.2007 11:42:12 by kunina
 * last modified $Date: 2015-04-17 16:47:40 +0300 (Пт., 17 апр. 2015) $ by $Author: afevma $
 * (C) ООО Крипто-Про 2004-2007.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package CMS_samples;

import com.objsys.asn1j.runtime.*;

import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.*;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;


/**
 * CMS sign and verify
 * <p/>
 * csptest -sfsign -in data.txt -my key -sign -out data.sgn
 * <p/>
 * csptest -sfsign -my key -verify -in data.sgn
 * <p/>
 * csptest -lowsign -in data.txt -my key -sign -out data_low.sgn
 * <p/>
 * csptest -lowsign -in data.sgn -verify ???
 *
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class CMS {

private static final String CMS_FILE = "cms_data_sgn";
private static final String CMS_FILE_PATH =
    CMStools.TEST_PATH + CMStools.SEPAR + CMS_FILE + CMStools.CMS_EXT;
private static final String CMS_FILE_PATH_2012_256 =
    CMStools.TEST_PATH + CMStools.SEPAR + CMS_FILE + "_2012_256" + CMStools.CMS_EXT;
private static final String CMS_FILE_PATH_2012_512 =
    CMStools.TEST_PATH + CMStools.SEPAR + CMS_FILE + "_2012_512" + CMStools.CMS_EXT;

/**
 * detached
 */
private static final String CMS_FILE_D = "cms_data_d_sgn";
private static final String CMS_FILE_D_PATH =
    CMStools.TEST_PATH + CMStools.SEPAR + CMS_FILE_D + CMStools.CMS_EXT;
private static final String CMS_FILE_D_PATH_2012_256 =
    CMStools.TEST_PATH + CMStools.SEPAR + CMS_FILE_D + "_2012_256" + CMStools.CMS_EXT;
private static final String CMS_FILE_D_PATH_2012_512 =
    CMStools.TEST_PATH + CMStools.SEPAR + CMS_FILE_D + "_2012_512" + CMStools.CMS_EXT;

 /**
 * Конструктор.
 *
 */
private CMS() {
    ;
}

/**
 * main Sign / Verify
 *
 * @param args //
 * @throws Exception e
 */
public static void main(String[] args) throws Exception {

    // ГОСТ Р 34.10-2001
//    main(CMStools.SIGN_KEY_NAME, CMStools.SIGN_KEY_PASSWORD, CMS_FILE_PATH,
//        CMS_FILE_D_PATH, JCP.GOST_DIGEST_OID, JCP.GOST_EL_SIGN_OID,
//            JCP.GOST_EL_SIGN_NAME, JCP.PROVIDER_NAME);

    // ГОСТ Р 34.10-2012 (256)
//    main(CMStools.SIGN_KEY_NAME_2012_256, CMStools.SIGN_KEY_PASSWORD_2012_256,
//        CMS_FILE_PATH_2012_256, CMS_FILE_D_PATH_2012_256, JCP.GOST_DIGEST_2012_256_OID,
//            JCP.GOST_SIGN_2012_256_OID, JCP.GOST_SIGN_2012_256_NAME, JCP.PROVIDER_NAME);

    // ГОСТ Р 34.10-2012 (512)
    main(CMStools.SIGN_KEY_NAME_2012_512, CMStools.SIGN_KEY_PASSWORD_2012_512,
        CMS_FILE_PATH_2012_512, CMS_FILE_D_PATH_2012_512, JCP.GOST_DIGEST_2012_512_OID,
            JCP.GOST_SIGN_2012_512_OID, JCP.GOST_SIGN_2012_512_NAME, JCP.PROVIDER_NAME);

}

/**
 * Выполнение подписи и проверки.
 *
 * @param alias Алиас ключа.
 * @param password Пароль к ключу.
 * @param attachedOutFile Имя файла для создания присоединенной подписи.
 * @param detachedOutFile Имя файла для создания отсоединенной подписи.
 * @param digestOid OID алгоритма хеширования.
 * @param signOid OID алгоритма подписи.
 * @param signAlg Алгоритм подписи.
 * @param providerName Имя провайдера.
 * @throws Exception
 */
private static void main(String alias, char[] password,
    String attachedOutFile, String detachedOutFile, String digestOid,
    String signOid, String signAlg, String providerName) throws Exception {

    alias = "RaUser-079809c4-f600-4a94-bf48-706e8b891a1e";
    password = "123qweASD".toCharArray();
    // prepare to sign
    final byte[] data = Array.readFile(CMStools.DATA_FILE_PATH);
    final PrivateKey key = CMStools.loadKey(alias, password);
    final Certificate cert = CMStools.loadCertificate(alias);

    /*
    //-ATTACHED-

    //Sign
    Array.writeFile( attachedOutFile, CMSSignEx(data, key, cert, false,
        digestOid, signOid, signAlg, providerName) );

    //Verify
    CMSVerifyEx(Array.readFile(attachedOutFile), cert, null,
        digestOid, signAlg, providerName);

    //-DETACHED-
*/

    //Sign
    Array.writeFile( detachedOutFile, CMSSignEx(data, key, cert, true,
        digestOid, signOid, signAlg, providerName) );

    //Verify
    CMSVerifyEx(Array.readFile(detachedOutFile), cert, data,
        digestOid, signAlg, providerName);

}

//----------------------------------------------------------------------------------------------------------------------

/**
 * sign CMS
 *
 * @param data data
 * @param key key
 * @param cert cert
 * @throws Exception e
 */
public static byte[] CMSSign(byte[] data, PrivateKey key,
    Certificate cert, boolean detached) throws Exception {
    return CMSSignEx(data, key, cert, detached, CMStools.DIGEST_OID,
        CMStools.SIGN_OID, JCP.GOST_DHEL_SIGN_NAME, JCP.PROVIDER_NAME);
}

/**
 * sign CMS
 *
 * @param data data
 * @param key key
 * @param cert cert
 * @param detached detached signature
 * @param digestOid digest algorithm OID
 * @param signOid signature algorithm OID
 * @param signAlg signature algorithm name
 * @param providerName provider name
 * @throws Exception e
 * @since 2.0
 */
public static byte[] CMSSignEx(byte[] data, PrivateKey key,
    Certificate cert, boolean detached, String digestOid,
    String signOid, String signAlg, String providerName)
    throws Exception {

    // sign
    final Signature signature = Signature.getInstance(signAlg, providerName);
    signature.initSign(key);
    signature.update(data);

    final byte[] sign = signature.sign();

    // create cms format
    return createCMSEx(data, sign, cert, detached, digestOid, signOid);
}

/**
 * createCMS
 *
 * @param buffer buffer
 * @param sign sign
 * @param cert cert
 * @param detached detached signature
 * @return byte[]
 * @throws Exception e
 */
public static byte[] createCMS(byte[] buffer, byte[] sign,
    Certificate cert, boolean detached) throws Exception {
    return createCMSEx(buffer, sign, cert, detached,
        CMStools.DIGEST_OID, CMStools.SIGN_OID);
}

/**
 * createCMS
 *
 * @param buffer buffer
 * @param sign sign
 * @param cert cert
 * @param detached detached signature
 * @param digestOid digest algorithm OID (to append to CMS)
 * @param signOid signature algorithm OID (to append to CMS)
 * @return byte[]
 * @throws Exception e
 * @since 2.0
 */
public static byte[] createCMSEx(byte[] buffer, byte[] sign,
    Certificate cert, boolean detached, String digestOid,
    String signOid) throws Exception {

    final ContentInfo all = new ContentInfo();
    all.contentType = new Asn1ObjectIdentifier(
        new OID(CMStools.STR_CMS_OID_SIGNED).value);

    final SignedData cms = new SignedData();
    all.content = cms;
    cms.version = new CMSVersion(1);

    // digest
    cms.digestAlgorithms = new DigestAlgorithmIdentifiers(1);
    final DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier(
        new OID(digestOid).value);

    a.parameters = new Asn1Null();
    cms.digestAlgorithms.elements[0] = a;

    if (detached) {
        cms.encapContentInfo = new EncapsulatedContentInfo(
            new Asn1ObjectIdentifier(
                new OID(CMStools.STR_CMS_OID_DATA).value), null);
    } // if
    else {
        cms.encapContentInfo =
            new EncapsulatedContentInfo(new Asn1ObjectIdentifier(
                new OID(CMStools.STR_CMS_OID_DATA).value),
                new Asn1OctetString(buffer));
    } // else

    // certificate
    cms.certificates = new CertificateSet(1);
    final ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate certificate =
        new ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate();
    final Asn1BerDecodeBuffer decodeBuffer =
        new Asn1BerDecodeBuffer(cert.getEncoded());
    certificate.decode(decodeBuffer);

    cms.certificates.elements = new CertificateChoices[1];
    cms.certificates.elements[0] = new CertificateChoices();
    cms.certificates.elements[0].set_certificate(certificate);

    // signer info
    cms.signerInfos = new SignerInfos(1);
    cms.signerInfos.elements[0] = new SignerInfo();
    cms.signerInfos.elements[0].version = new CMSVersion(1);
    cms.signerInfos.elements[0].sid = new SignerIdentifier();

    final byte[] encodedName = ((X509Certificate) cert)
        .getIssuerX500Principal().getEncoded();
    final Asn1BerDecodeBuffer nameBuf = new Asn1BerDecodeBuffer(encodedName);
    final Name name = new Name();
    name.decode(nameBuf);

    final CertificateSerialNumber num = new CertificateSerialNumber(
        ((X509Certificate) cert).getSerialNumber());
    cms.signerInfos.elements[0].sid.set_issuerAndSerialNumber(
        new IssuerAndSerialNumber(name, num));
    cms.signerInfos.elements[0].digestAlgorithm =
        new DigestAlgorithmIdentifier(new OID(digestOid).value);
    cms.signerInfos.elements[0].digestAlgorithm.parameters = new Asn1Null();
    cms.signerInfos.elements[0].signatureAlgorithm =
        new SignatureAlgorithmIdentifier(new OID(signOid).value);
    cms.signerInfos.elements[0].signatureAlgorithm.parameters = new Asn1Null();
    cms.signerInfos.elements[0].signature = new SignatureValue(sign);

/*
    // --> pavel
    //
    //-Time
    //
    int kmax = 2, k = 0;
    cms.signerInfos.elements[0].unsignedAttrs = new UnsignedAttributes(kmax);
    cms.signerInfos.elements[0].unsignedAttrs.elements[k] = new Attribute(new OID("1.2.840.113549.1.9.5").value,
            new Attribute_values(1));
    final Time time = new Time();
    final Asn1UTCTime UTCTime = new Asn1UTCTime();
    UTCTime.setTime(Calendar.getInstance());
    time.set_utcTime(UTCTime);
    cms.signerInfos.elements[0].unsignedAttrs.elements[k].values.elements[0] = time.getElement();
    k++;
    //
    // тип содержимого OID = 1.2.840.113549.1.9.3
    //
    cms.signerInfos.elements[0].unsignedAttrs.elements[k] =
            new Attribute(new OID(CMStools.STR_CMS_OID_CONT_TYP_ATTR).value, new Attribute_values(1));
    final Asn1Type conttype = new Asn1ObjectIdentifier(new OID(CMStools.STR_CMS_OID_DATA).value);
    cms.signerInfos.elements[0].unsignedAttrs.elements[k].values.elements[0] = conttype;


    //
    //  --< pavel
    //
*/



    // encode
    final Asn1BerEncodeBuffer asnBuf = new Asn1BerEncodeBuffer();
    all.encode(asnBuf, true);
    return asnBuf.getMsgCopy();
}

/**
 * verify CMS
 *
 * @param buffer buffer
 * @param cert cert
 * @param data data
 * @throws Exception e
 * @deprecated начиная с версии 1.0.54, следует использовать
 * функцонал CAdES API (CAdES.jar)
 */
public static void CMSVerify(byte[] buffer, Certificate cert,
    byte[] data) throws Exception {
    CMSVerifyEx(buffer, cert, data, CMStools.DIGEST_OID,
        JCP.GOST_EL_SIGN_NAME, JCP.PROVIDER_NAME);
}

/**
 * verify CMS
 *
 * @param buffer buffer
 * @param cert cert
 * @param data data
 * @param digestOidValue digest algorithm OID
 * @param signAlg signature algorithm name
 * @param providerName provider name
 * @throws Exception e
 * @deprecated начиная с версии 1.0.54, следует использовать
 * функцонал CAdES API (CAdES.jar)
 * @since 2.0
 */
public static void CMSVerifyEx(byte[] buffer, Certificate cert,
    byte[] data, String digestOidValue, String signAlg,
    String providerName) throws Exception {

    int i;
    final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(buffer);
    final ContentInfo all = new ContentInfo();
    all.decode(asnBuf);

    if (!new OID(CMStools.STR_CMS_OID_SIGNED).eq(all.contentType.value)) {
        throw new Exception("Not supported");
    } // if

    final SignedData cms = (SignedData) all.content;
    if (cms.version.value != 1) {
        throw new Exception("Incorrect version");
    } // if

    if (!new OID(CMStools.STR_CMS_OID_DATA).eq(
        cms.encapContentInfo.eContentType.value)) {
        throw new Exception("Nested not supported");
    } // if

    byte[] text = null;
    if (data != null) {
        text = data;
    } // if
    else if (cms.encapContentInfo.eContent != null) {
        text = cms.encapContentInfo.eContent.value;
    } // else

    if (text == null) {
        throw new Exception("No content");
    } // if

    OID digestOid = null;
    DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier(
        new OID(digestOidValue).value);

    for (i = 0; i < cms.digestAlgorithms.elements.length; i++) {
        if (cms.digestAlgorithms.elements[i].algorithm.equals(a.algorithm)) {
            digestOid = new OID(cms.digestAlgorithms.elements[i].algorithm.value);
            break;
        } // if
    } // for

    if (digestOid == null) {
        throw new Exception("Unknown digest");
    } // if

    int pos = -1;

    if (cms.certificates != null) {

        for (i = 0; i < cms.certificates.elements.length; i++) {

            final Asn1BerEncodeBuffer encBuf = new Asn1BerEncodeBuffer();
            cms.certificates.elements[i].encode(encBuf);
            final byte[] in = encBuf.getMsgCopy();

            if (Arrays.equals(in, cert.getEncoded())) {
                System.out.println("Certificate: " + ((X509Certificate)cert).getSubjectDN());
                pos = i;
                break;
            } // if

        } // for

        if (pos == -1) {
            throw new Exception("Not signed on certificate.");
        } // if

    }
    else if (cert == null) {
        throw new Exception("No certificate found.");
    } // else
    else {
        // Если задан {@link #cert}, то пробуем проверить
        // первую же подпись на нем.
        pos = 0;
    } // else

    final SignerInfo info = cms.signerInfos.elements[pos];
    if (info.version.value != 1) {
        throw new Exception("Incorrect version");
    } // if

    if (!digestOid.equals(new OID(info.digestAlgorithm.algorithm.value))) {
        throw new Exception("Not signed on certificate.");
    } // if

    final byte[] sign = info.signature.value;

    // check
    final Signature signature = Signature.getInstance(signAlg, providerName);
    signature.initVerify(cert);
    signature.update(text);

    final boolean checkResult = signature.verify(sign);
    if (checkResult) {
        if (CMStools.logger != null) {
            CMStools.logger.info("Valid signature");
        }
    } // if
    else {
        throw new Exception("Invalid signature.");
    } // else

}
}
