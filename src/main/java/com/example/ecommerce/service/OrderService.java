package com.example.ecommerce.service;

import com.example.ecommerce.model.Address;
import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.OrderShippingAddress;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.SellerOrder;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.repository.AppUserRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.web.dto.CreateOrderRequest;
import com.example.ecommerce.web.dto.OrderItemRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final AppUserRepository appUserRepository;
    private final AddressRepository addressRepository;

    public OrderService(
        OrderRepository orderRepository,
        ProductService productService,
        AppUserRepository appUserRepository,
        AddressRepository addressRepository
    ) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.appUserRepository = appUserRepository;
        this.addressRepository = addressRepository;
    }

    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        AppUser buyer = appUserRepository.findById(request.getBuyerId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));
        Address address = addressRepository.findById(request.getShippingAddressId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipping address not found"));
        if (!address.getUser().getId().equals(buyer.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shipping address does not belong to buyer");
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setShippingAddress(snapshotAddress(address));

        BigDecimal total = BigDecimal.ZERO;
        Map<Long, SellerOrder> sellerOrders = new HashMap<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productService.getProduct(itemRequest.getProductId());
            if (product.getSeller() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product has no seller");
            }
            productService.reduceStock(product, itemRequest.getQuantity());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(productService.priceFor(product));

            SellerOrder sellerOrder = sellerOrders.computeIfAbsent(product.getSeller().getId(), sellerId -> {
                SellerOrder created = new SellerOrder();
                created.setSeller(product.getSeller());
                created.setTotalAmount(BigDecimal.ZERO);
                return created;
            });
            item.setSellerOrder(sellerOrder);
            sellerOrder.setTotalAmount(sellerOrder.getTotalAmount().add(
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            ));

            order.addItem(item);
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        for (SellerOrder sellerOrder : sellerOrders.values()) {
            order.addSellerOrder(sellerOrder);
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    private OrderShippingAddress snapshotAddress(Address address) {
        OrderShippingAddress shippingAddress = new OrderShippingAddress();
        String name = address.getUser().getFirstName() == null ? "" : address.getUser().getFirstName();
        if (address.getUser().getLastName() != null) {
            name = (name + " " + address.getUser().getLastName()).trim();
        }
        shippingAddress.setName(name.isEmpty() ? "Delivery" : name);
        shippingAddress.setLine1(address.getLine1());
        shippingAddress.setLine2(address.getLine2());
        shippingAddress.setCity(address.getCity());
        shippingAddress.setRegion(address.getRegion());
        shippingAddress.setPostalCode(address.getPostalCode());
        shippingAddress.setCountry(address.getCountry());
        shippingAddress.setPhoneNumber(address.getPhoneNumber());
        return shippingAddress;
    }
}
