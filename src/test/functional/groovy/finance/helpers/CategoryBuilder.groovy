package finance.helpers

import finance.domain.Category

class CategoryBuilder {

    String categoryName = "foo"

    static CategoryBuilder builder() {
        return new CategoryBuilder()
    }

    Category build() {
        Category category = new Category()
        category.category = categoryName
        return category
    }

    CategoryBuilder category(categoryName) {
        this.categoryName = categoryName
        return this
    }
}