//package finance.services
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import finance.domain.Category
//import finance.domain.Description
//import graphql.schema.DataFetcher
//import org.apache.logging.log4j.LogManager
//import org.apache.logging.log4j.Logger
//import org.springframework.security.core.Authentication
//import org.springframework.security.core.context.SecurityContextHolder
//import org.springframework.stereotype.Service
//
//@Service
//class GraphQLDataFetchers(
//    private val descriptionService: DescriptionService,
//    private val categoryService: CategoryService
//) {
//    val descriptions: DataFetcher<List<Description>>
//        get() = DataFetcher<List<Description>> {
//            val username = SecurityContextHolder.getContext().authentication.name
//            val authentication: Authentication = SecurityContextHolder.getContext().authentication
//            logger.info(authentication.isAuthenticated)
//            logger.info(username)
//           // val page: String = it.getArgument("page")
//           //     .orElse(AppConstants.DEFAULT_PAGE_NUMBER) as String?. toInt ()
//           // val size: Int =
//           //     Optional.ofNullable(env.getArgument("size")).orElse(AppConstants.DEFAULT_PAGE_SIZE) as String?. toInt ()
//            return@DataFetcher descriptionService.fetchAllDescriptions()
//        }
//
//    val description: DataFetcher<Description>
//        get() = DataFetcher<Description> {
//            val description = descriptionService.findByDescriptionName(it.getArgument("descriptionName")).get()
//            description
//        }
//
//    fun createCategory(): DataFetcher<Category> {
//        return DataFetcher<Category> {
//            val categoryName: String = it.getArgument("category")
//            logger.info(categoryName)
//            val category = Category()
//            category.categoryName = categoryName
//
//            categoryService.insertCategory(category)
//        }
//    }
//
//    fun createDescription(): DataFetcher<Description> {
//        return DataFetcher<Description> {
//            val descriptionName: String = it.getArgument("description")
//            logger.info(description)
//            val description = Description()
//            description.descriptionName = descriptionName
//
//            descriptionService.insertDescription(description)
//        }
//    }
//
//    val categories: DataFetcher<List<Category>>
//        get() = DataFetcher<List<Category>> {
//            return@DataFetcher categoryService.categories()
//        }
//
//
//    companion object {
//        val mapper = ObjectMapper()
//        val logger: Logger = LogManager.getLogger()
//    }
//}