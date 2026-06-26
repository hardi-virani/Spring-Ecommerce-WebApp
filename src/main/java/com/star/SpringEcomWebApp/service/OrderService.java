//general idea (get confirmation from AI) : when you want to get ot set about list which is connected to other DTO or model, you need to write a for lop or somthing like that.

package com.star.SpringEcomWebApp.service;

import com.star.SpringEcomWebApp.model.Order;
import com.star.SpringEcomWebApp.model.OrderItem;
import com.star.SpringEcomWebApp.model.Product;
import com.star.SpringEcomWebApp.model.dto.OrderItemRequest;
import com.star.SpringEcomWebApp.model.dto.OrderItemResponse;
import com.star.SpringEcomWebApp.model.dto.OrderRequest;
import com.star.SpringEcomWebApp.model.dto.OrderResponse;
import com.star.SpringEcomWebApp.repo.OrderRepo;
import com.star.SpringEcomWebApp.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class OrderService {

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private OrderRepo orderRepo;

    //Handles the complete flow of placing an order:
    //Created order, creates order items, updates product stock, saves order, and returns response DTO.
    public OrderResponse placeOrder(OrderRequest request) {

        Order order = new Order();

        //Generate a unique details using data coming from OrderRequest.
        String orderId = "ORD" + UUID.randomUUID().toString().substring(0,8).toUpperCase();

        //Set basic order details using data coming from OrderRequest.
        order.setOrderId(orderId);
        order.setCustomerName(request.customerName());
        order.setEmail(request.email());
        order.setStatus("PLACED");
        order.setOrderDate(LocalDate.now());

        //This list will store all OrderItem entities for this order.
        List<OrderItem> orderItems = new ArrayList<>();

        //Loop through each product requested by the user and convert it into an OrderItem.
        for(OrderItemRequest itemReq : request.items() ) {

            //Fetch the actual product from DB using product id from request.
            Product product = productRepo.findById(itemReq.productId()).orElseThrow(() -> new RuntimeException("Product not found"));

            //Reduce product stock based on ordered quantity.
            product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());

            // Save updated product stock back to DB.
            productRepo.save(product);


            //Create one OrderItem containing product, quantity, total price and linked order.
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())))
                    .order(order)
                    .build();

            // Add this item to the order item list.
            orderItems.add(orderItem);
        }

        // Attach all order items to the main order before saving.
        order.setOrderItems(orderItems);

        //  Save the complete order into DB.
        Order savedOrder = orderRepo.save(order);

        // This list will store clean response DTOs for each order item.
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        // Convert OrderItem entities into OrderItemResponse DTOs for frontend/Postman response.
        for(OrderItem item : order.getOrderItems()) {

            // Create response object with only the data client needs.
            OrderItemResponse orderItemResponse = new OrderItemResponse(
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getTotalPrice()
            );

            // Add this item response to final response list.
            itemResponses.add(orderItemResponse);
        }

        // Create final response DTO containing order details and item responses.
        OrderResponse orderResponse = new OrderResponse(
                savedOrder.getOrderId(),
                savedOrder.getCustomerName(),
                savedOrder.getEmail(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                itemResponses
                );

        // Return clean order response to frontend/Postman.
        return orderResponse;
    }

    // Fetch all orders from the DB and convert them into OrderResponse DTOs
    public List<OrderResponse> getAllOrdersResponses() {

        // Get all Order entitites stored in the DB.
        List<Order> orders = orderRepo.findAll();

        //This list will store the final respsonse DTOs returned to the client.
        List<OrderResponse> orderResponses = new ArrayList<>(); // we also need the object of list of orderResponses. Because this is what we are going to send right now. in return.

        // Loop through every order fetched from the DB.
        for(Order order : orders) {

            // This list will store response DTOs for items belonging to the current order.
            List<OrderItemResponse> itemResponses = new ArrayList<>();

            // Convert each OrderItem entity into OrderItemResponse DTO.
            for(OrderItem item : order.getOrderItems()) {

                // Create a response object containing only the required item details
                OrderItemResponse orderItemResponse = new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getTotalPrice()
                );
                // Add the converted item to the current order's response list.
                itemResponses.add(orderItemResponse);
            }

            // Create a response DTO for the current order.
            OrderResponse orderResponse = new OrderResponse(
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getEmail(),
                    order.getStatus(),
                    order.getOrderDate(),
                    itemResponses
            );

            // Add the current order respoonse to the final response list.
            orderResponses.add(orderResponse);

        }

        // Return all orders in a clean response format.
        return orderResponses;
    }
}
