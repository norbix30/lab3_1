package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import canonicalmodel.publishedlanguage.ClientDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

@ExtendWith(MockitoExtension.class)
class BookKeeperTest {
    @Mock
    private TaxPolicy taxPolicy;

    private BookKeeper bookKeeper;
    private InvoiceRequest invoiceRequest;
    private RequestItemBuilder requestItemBuilder;

    @BeforeEach
    void setUp() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(new ClientDataBuilder().build());
        requestItemBuilder = new RequestItemBuilder();

        lenient().when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class)))
                .thenReturn(new TaxBuilder().build());
    }

    @Test
    void emptyInvoiceRequest_shouldReturnInvoiceWithNoItems() {
        Invoice resultInvoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertEquals(0, resultInvoice.getItems().size());
    }

    @Test
    void emptyInvoiceRequest_shouldNotCallCalculateTaxMethod() {
        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(0))
                .calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    void singleItemInvoiceRequest_ShouldReturnInvoiceWithOneItem() {
        RequestItem requestItem = requestItemBuilder.build();
        invoiceRequest.add(requestItem);

        Invoice resultInvoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertEquals(1, resultInvoice.getItems().size());
    }

    @Test
    void twoItemsInvoiceRequest_ShouldCallCalculateTaxMethodTwice() {
        for (int i = 0; i < 2; i++) {
            RequestItem requestItem = requestItemBuilder.build();
            invoiceRequest.add(requestItem);
        }

        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2))
                .calculateTax(any(ProductType.class), any(Money.class));
    }
}
