import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.orderservice.entity.Order;
import java.util.List;          // For returning list of cart items
import java.time.LocalDateTime; // For Order createdAt

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private CartService cartService;

    public Order placeOrder(double total) {
        Order order = new Order();
        order.setTotal(total);
        order.setCreatedAt(LocalDateTime.now());

        cartService.clearCart(); // clear cart after order
        return orderRepo.save(order);
    }
}
