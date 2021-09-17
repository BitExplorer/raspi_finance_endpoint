package finance.repositories

import finance.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

//@Repository(value = "categoryRepository")
interface CategoryRepository : JpaRepository<Category, Long> {

    fun findByCategoryName(categoryName: String): Optional<Category>

    fun findByActiveStatusOrderByCategoryName(activeStatus: Boolean): List<Category>
}