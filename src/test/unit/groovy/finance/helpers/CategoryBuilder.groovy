package finance.helpers


import finance.domain.Category

import java.sql.Timestamp

class CategoryBuilder {
    String categoryName = 'foo'

    static CategoryBuilder builder() {
        return new CategoryBuilder()
    }

    Category build() {
        Category category = new Category()
        category.category = categoryName
        return category
    }

    CategoryBuilder category(category) {
        this.categoryName = category
        return this
    }
}
