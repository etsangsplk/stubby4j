package org.stubby.handlers;

import org.eclipse.jetty.server.Request;
import org.stubby.database.DataStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 7/4/12, 1:54 AM
 */
public final class SslHandler extends ClientHandler {

   public SslHandler(final DataStore dataStore) {
      super(dataStore);
   }

   @Override
   public void handle(final String target,
                      final Request baseRequest,
                      final HttpServletRequest request,
                      final HttpServletResponse response) throws IOException, ServletException {

      super.handle(target, baseRequest, request, response);
   }
}