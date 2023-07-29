package org.stc;

import lombok.Getter;
import lombok.Setter;
import sun.net.www.http.HttpClient;

import javax.swing.text.Document;
import javax.swing.text.html.parser.Entity;
import java.io.Closeable;
import java.security.cert.Certificate;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final String URL = "htt://<server-name>[:server-port]" +
            "/api/v2/{extension}/ rollout?omsId={omsId}";
    private final String CLIENT_TOCEN = "clientTocen";
    private final String USER_NAME = "userName";

    private int requestLimit;
    private final TimeUnit timeUnit;
    private static int counter;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        if (requestLimit >= 0) {
            this.requestLimit = requestLimit;
            counter = requestLimit;
        } else {
            throw new IllegalArgumentException("Передано отрицательное число");
        }
    }

    public void runRequest(Document document, String signature) {
        String docJson = getDocJson(document, signature).toString();
        httpRequest(docJson);
    }

    @SuppressWarnings("unchecked")
    private JSONObject getDocJson(Document document, String signature) {
        JSONObect doc = new JSONObject();
        if (isNull(document.getDescription())) {
            JSONObject inn = new JSONObject();
            inn.put("participantInn", document.getParticipantInn());
            doc.put("description", inn);
        }
        doc.put("doc_id", document.getDocId());
        doc.put("doc_status", document.getDocStatus());
        doc.put("doc_type", document.getDocType());
        if (isNull(document.getImportRequest())) {
            doc.put("importRequest", document.getImportRequest());

        }
        doc.put("owner_inn", document.getOwnerInn());
        doc.put("participant_inn", document.getParticipantInn());
        doc.put("producer_inn", document.getProducerInn());
        doc.put("production_date", document.getProducerInn());
        doc.put("production_type", document.getProductionTtpe());
        Document.Products products = document.getProducts();
        if (product != null) {
            JSONArry productsList = new JSONArray();
            JSONObject products = new JSONObject();
            if (product.getCertificateDocument() != null) {
                products.put("certificate_document", products.getCertificateDocument());
            } else if (isNull(product.getCertificateDocumentDate())) {
                products.put("certificate_document_date", product.getCertificateDocumentDate());
            } else if (isNull(product.getCertificateDocumentNumber())) {
                products.put("certificate_document_number", product.getCertificateDocumentNumber());
            }
            products.put("owner_inn", document.getOwnerInn());
            products.put("producer_inn", document.getProducerInn());
            products.put("production_date", document.getProductionDate());
            if ((!document.getProductionDate().equals(product.getProductionDate))) {
                products.put("production_date", product.getProductionDate());
            }
            products.put("tnved_code", product.tnvedCode);
            if (isNull(product.getUitCode())) {
                products.put("uitu_code", product.getUituCode());
            } else {
                throw new IllegalArgumentException("Одно из полей uit_code/uitu_code" + "является обязательным");
            }
            productsList.add(products);
            doc.put("reg_number", document.getRegNumber());
            return doc;
        }

        private void httpRequest(String json) {
            if (requestLimit != 0) {
                synchronized (this) {
                    counter--;
                }
            }
            try {
                if (counter < 0) {
                    Thread.sleep(getTime());
                    counter = requestLimit;
                }
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(URL);

                StringEntity entity = new StringEntity(json);
                post.addHeader("content-type", "application/json");
                post.addHeader("clientTocen", CLIENT_TOCEN);
                post.addHeader("userName", USER_NAME);
                post.setEntity(entity);
                httpClient.execute(post);
                httpClient.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private boolean isNull(String check) {
            return check != null;
        }

        public enum TimeUnit {
            SECOND, MINUTE, HOUR
        }

        private long getTime() {
            return switch (timeUnit) {
                case SECOND -> 1000;
                case MINUTE -> 1000*60;
                case HOUR -> 1000*60*60;
            };
        }

        public static class Document {

            @Getter
            @Setter
            private String description;
            @Getter
            private final String participantInn;
            @Getter
            private final String docId;
            @Getter
            private  final String docStatus;
            @Getter
            private  final  String docType;
            @Getter
            @Setter
            private String impirtRequest;
            @Getter
            private final String ownerInn;
            @Getter
            private final String producerInn;
            @Getter
            private final String productionDate;
            @Getter
            private final String productionType;
            @Getter
            private final String regDate;
            @Getter
            private final String regNumber;
            @Getter
            @Setter
            private Products products;

            public Document(String participantInn, String docId, String docStatus,
                            String docType, String ownerInn, String producerInn,
                            String getProductionDate, String getProductionType,
                            String regDate, String regNumber) {
                this.participantInn = participantInn;
                this.docId = docId;
                this.docStatus = docStatus;
                this.docType = docType;
                this.ownerInn = ownerInn;
                this.producerInn = producerInn;
                this.productionDate = getProductionDate;
                this.productionType = getProductionType;
                this.regDate = regDate;
                this.regNumber = regNumber;
            }

            public class Products {
                @Getter
                @Setter
                private CertificateTupe certificateTupe;
                @Getter
                @Setter
                private String certificateDocumentDate;
                @Getter
                @Setter
                private String certificateDocumentNumber;
                @Getter
                @Setter
                private String productiondate;
                @Getter
                @Setter
                private String tvnedCode;
                @Getter
                @Setter
                private String uitCode;
                @Getter
                @Setter
                private String uituCode;

                public enum CertificateType {
                    CONFORMITY_CERTIFICATE, CONFORMITY_DECLARATION
                }
            }
        }



    }
}
