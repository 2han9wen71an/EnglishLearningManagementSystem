package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.Book;
import com.chun.myspringboot.service.Impl.BookServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 书籍API控制器
 */
@RestController
@RequestMapping("/api/books")
public class BookApiController {
    
    @Autowired
    private BookServiceImpl bookService;
    
    /**
     * 获取所有书籍
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Book>>> getAllBooks() {
        List<Book> books = bookService.queryAllBook();
        return ResponseEntity.ok(ApiResponse.success(books));
    }
    
    /**
     * 获取书籍详情
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Book>> getBookById(@PathVariable Integer bookId) {
        Book book = bookService.queryBookById(bookId);
        if (book != null) {
            return ResponseEntity.ok(ApiResponse.success(book));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("书籍不存在"));
        }
    }
    
    /**
     * 管理员：添加书籍
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Book>> addBook(@RequestBody Book book) {
        // 这里需要添加一个新的方法来添加书籍
        // 假设已经存在这个方法
        int result = bookService.addBook(book);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("书籍添加成功", book));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }
    
    /**
     * 管理员：更新书籍
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<String>> updateBook(@PathVariable Integer bookId, @RequestBody Book book) {
        // 确保bookId匹配
        if (!bookId.equals(book.getBookId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("书籍ID不匹配"));
        }
        
        // 检查书籍是否存在
        Book existingBook = bookService.queryBookById(bookId);
        if (existingBook == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("书籍不存在"));
        }
        
        // 这里需要添加一个新的方法来更新书籍
        // 假设已经存在这个方法
        int result = bookService.updateBook(book);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("书籍更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }
    
    /**
     * 管理员：删除书籍
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable Integer bookId) {
        // 检查书籍是否存在
        Book existingBook = bookService.queryBookById(bookId);
        if (existingBook == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("书籍不存在"));
        }
        
        // 这里需要添加一个新的方法来删除书籍
        // 假设已经存在这个方法
        int result = bookService.deleteBook(bookId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("书籍删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }
}
