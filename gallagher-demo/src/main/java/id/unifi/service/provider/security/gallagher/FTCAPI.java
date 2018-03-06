package id.unifi.service.provider.security.gallagher;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.*;
import org.jinterop.dcom.impls.JIObjectFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTCAPI implements IFTExternalEventInput2, Closeable {

    private final String UUID_FTEIAPI = "C56CA66E-FF0E-4581-B8F3-63F9725D2EC9";
    private final String UUID_IFTEXTERNALEVENTINPUT  = "304B34B7-69F0-4429-B96D-840431E1275C";
    private final String UUID_IFTEXTERNALEVENTINPUT2 = "C06D6E49-9FE5-4C1C-8CFE-AA9763CCDDAB";
    private final IJIComObject ftceapi;

    public FTCAPI(
            String server,
            String domain,
            String username,
            String password
    ) {

        final JISession session = JISession.createSession(
                domain,
                username,
                password);
        try {
            final JIComServer comServer = new JIComServer(
                    JIClsid.valueOf(UUID_FTEIAPI),
                    server,
                    session);
            this.ftceapi = comServer.createInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // FTCAPI
    ///////////////////////////////////////////////////////////////////////////

    public void registerMiddleware(IFTMiddleware2 middleware) {


        final JILocalCoClass coClass = new JILocalCoClass(
                new JILocalInterfaceDefinition(IFTMiddleware2DcomBridge.CLSID, false),
                new IFTMiddleware2DcomBridge(middleware));

        JILocalInterfaceDefinition interfaceDefinition =
                coClass.getInterfaceDefinition();
        interfaceDefinition.addMethodDescriptor(notifyItemRegisteredMD());
        interfaceDefinition.addMethodDescriptor(notifyItemDeregisteredMD());
        interfaceDefinition.addMethodDescriptor(notifySystemRegisteredMD());
        interfaceDefinition.addMethodDescriptor(notifySystemDeregisteredMD());
        interfaceDefinition.addMethodDescriptor(notifyAlarmAcknowledgedMD());

        ArrayList<String> supportedInterfaces = new ArrayList<>();
        supportedInterfaces.add(IFTMiddleware2DcomBridge.CLSID);
        coClass.setSupportedEventInterfaces(supportedInterfaces);

        try {
            final IJIComObject remoteMiddleware = ftceapi.queryInterface(
                    UUID_IFTEXTERNALEVENTINPUT);
            JIObjectFactory.attachEventHandler(
                    remoteMiddleware,
                    IFTMiddleware2DcomBridge.CLSID,
                    JIObjectFactory.buildObject(ftceapi.getAssociatedSession(), coClass));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private JILocalMethodDescriptor notifyItemRegisteredMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        return new JILocalMethodDescriptor("notifyItemRegistered", params);
    }

    private JILocalMethodDescriptor notifyItemDeregisteredMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        return new JILocalMethodDescriptor("notifyItemDeregistered", params);
    }

    private JILocalMethodDescriptor notifySystemRegisteredMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        return new JILocalMethodDescriptor("notifySystemRegistered", params);
    }

    private JILocalMethodDescriptor notifySystemDeregisteredMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        return new JILocalMethodDescriptor("notifySystemDeregistered", params);
    }

    private JILocalMethodDescriptor notifyAlarmAcknowledgedMD() {
        final JILocalParamsDescriptor params = new JILocalParamsDescriptor();
        params.addInParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR), JIFlags.FLAG_NULL);
        params.addInParamAsObject(JIUnsignedInteger.class, JIFlags.FLAG_REPRESENTATION_UNSIGNED_INT);
        return new JILocalMethodDescriptor("notifyAlarmAcknowledged", params);
    }

    ///////////////////////////////////////////////////////////////////////////
    // IFTExternalEventInput2
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void logEvent(
            int eventType,
            int eventId,
            Date eventTime,
            boolean hasRestoral,
            String systemId,
            String itemId,
            String message,
            String details
    ) {
        double timestamp = (new OleDate(eventTime)).toDouble();
        JICallBuilder cb = new JICallBuilder(true);
        cb.setOpnum(0);
        cb.addInParamAsInt(eventType, JIFlags.FLAG_NULL);
        cb.addInParamAsInt(eventId, JIFlags.FLAG_NULL);
        cb.addInParamAsDouble(timestamp, JIFlags.FLAG_NULL);
        cb.addInParamAsBoolean(hasRestoral, JIFlags.FLAG_NULL);
        cb.addInParamAsString(systemId, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        cb.addInParamAsString(itemId, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        cb.addInParamAsString(message, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        cb.addInParamAsString(details, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        try {
            IJIComObject eei2 = ftceapi.queryInterface(UUID_IFTEXTERNALEVENTINPUT2);
            eei2.call(cb);
        } catch (JIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logCardEvent(
            int eventType,
            int eventId,
            Date eventTime,
            boolean hasRestoral,
            int cardNumber,
            int facilityCode,
            String systemId,
            String itemId,
            String message,
            String details
    ) {
        double timestamp = (new OleDate(eventTime)).toDouble();
        JICallBuilder cb = new JICallBuilder(true);
        cb.setOpnum(1);
        cb.addInParamAsInt(eventType, JIFlags.FLAG_NULL);
        cb.addInParamAsInt(eventId, JIFlags.FLAG_NULL);
        cb.addInParamAsDouble(timestamp, JIFlags.FLAG_NULL);
        cb.addInParamAsBoolean(hasRestoral, JIFlags.FLAG_NULL);
        cb.addInParamAsInt(cardNumber, JIFlags.FLAG_NULL);
        cb.addInParamAsInt(facilityCode, JIFlags.FLAG_NULL);
        cb.addInParamAsString(systemId, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        cb.addInParamAsString(itemId, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        cb.addInParamAsString(message, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        cb.addInParamAsString(details, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        try {
            IJIComObject eei2 = ftceapi.queryInterface(UUID_IFTEXTERNALEVENTINPUT2);
            eei2.call(cb);
        } catch (JIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyRestore(
            int eventType,
            String systemId,
            String itemId
    ) {
        JICallBuilder cb = new JICallBuilder(true);
        cb.setOpnum(2);
        cb.addInParamAsInt(eventType, JIFlags.FLAG_NULL);
        cb.addInParamAsString(systemId, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        cb.addInParamAsString(itemId, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        try {
            IJIComObject eei2 = ftceapi.queryInterface(UUID_IFTEXTERNALEVENTINPUT2);
            eei2.call(cb);
        } catch (JIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyStatus(
            String systemId,
            String itemId,
            int state,
            boolean tampered,
            boolean offline,
            String message
    ) {
        JICallBuilder cb = new JICallBuilder(true);
        cb.setOpnum(3);
        cb.addInParamAsString(systemId, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        cb.addInParamAsString(itemId, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        cb.addInParamAsInt(state, JIFlags.FLAG_REPRESENTATION_UNSIGNED_INT);
        cb.addInParamAsBoolean(tampered, JIFlags.FLAG_REPRESENTATION_UNSIGNED_INT);
        cb.addInParamAsBoolean(offline, JIFlags.FLAG_REPRESENTATION_UNSIGNED_INT);
        cb.addInParamAsString(message, JIFlags.FLAG_REPRESENTATION_STRING_BSTR);
        try {
            IJIComObject eei2 = ftceapi.queryInterface(UUID_IFTEXTERNALEVENTINPUT2);
            eei2.call(cb);
        } catch (JIException e) {
            throw new RuntimeException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closeable
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() throws IOException {
        try {
            if (ftceapi != null) {
                ftceapi.release();
            }
        } catch (JIException e) {
            // Eat it.
        }
    }

}
