package finance.helpers

import finance.domain.Category

class CategoryBuilder {
    String categoryName = 'foo'
    Boolean activeStatus = true

    static CategoryBuilder builder() {
        return new CategoryBuilder()
    }

    Category build() {
        Category category = new Category().with {
            categoryName = this.categoryName
            activeStatus = this.activeStatus
            return it
        }
        return category
    }

    CategoryBuilder withCategoryName(String categoryName) {
        this.categoryName = categoryName
        return this
    }

    CategoryBuilder withActiveStatus(Boolean activeStatus) {
        this.activeStatus = activeStatus
        return this
    }
}
