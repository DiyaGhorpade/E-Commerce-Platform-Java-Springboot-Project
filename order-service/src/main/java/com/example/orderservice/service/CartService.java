import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;          // For returning list of cart items
import java.time.LocalDateTime; // For Order createdAt

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartRepo;

    public CartItem addToCart(CartItem item) {
        return cartRepo.save(item);
    }

    public List<CartItem> getCartItems() {
        return cartRepo.findAll();
    }

    public void clearCart() {
        cartRepo.deleteAll();
    }
}
