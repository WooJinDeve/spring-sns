package com.sns.sns.domain.post.repository;

import com.sns.sns.domain.post.dto.DailyPostCount;
import com.sns.sns.domain.post.dto.DailyPostCountRequest;
import com.sns.sns.domain.post.entity.Post;
import com.sns.sns.util.PageHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PostRepository {
    private static final String TABLE = "Post";
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final SimpleJdbcInsert simpleJdbcInsert;

    private static final RowMapper<Post> ROW_MAPPER = (ResultSet resultSet, int rowNum) -> Post.builder()
            .id(resultSet.getLong("id"))
            .memberId(resultSet.getLong("memberId"))
            .contents(resultSet.getString("contents"))
            .createdDate(resultSet.getObject("createdDate", LocalDate.class))
            .createdAt(resultSet.getObject("createdAt", LocalDateTime.class))
            .build();


    public PostRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(this.namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");
    }


    public Post save(Post post) {
        if (post.getId() == null)
            return insert(post);
        throw new UnsupportedOperationException("Post는 갱신을 지원하지 않습니다");
    }

    private Post insert(Post post) {
        var params = new BeanPropertySqlParameterSource(post);
        var id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Post.builder()
                .id(id)
                .memberId(post.getMemberId())
                .contents(post.getContents())
                .createdDate(post.getCreatedDate())
                .createdAt(post.getCreatedAt())
                .build();
    }

    public List<Post> findByMemberId(Long memberId) {
        var params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        var sql = String.format("SELECT * FROM `%s` WHERE id = :id", TABLE);
        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<DailyPostCount> groupByCreatedDate(DailyPostCountRequest request){
        var sql = String.format("""
                SELECT memberId, createdDate, count(id) as cnt 
                FROM %s 
                WHERE memberId = :memberId and createdDate between :firstDate and :lastDate 
                GROUP BY memberId, createdDate
                """, TABLE);
        var params = new BeanPropertySqlParameterSource(request);

        RowMapper<DailyPostCount> mapper = (ResultSet resultSet, int rowNum) -> new DailyPostCount(
                resultSet.getLong("memberId"),
                resultSet.getObject("createdDate", LocalDate.class),
                resultSet.getLong("cnt")
        );
        return namedParameterJdbcTemplate.query(sql, params, mapper);
    }

    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable){
        var params = new MapSqlParameterSource()
                .addValue("memberId",memberId)
                .addValue("size", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        var sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId = :memberId
                ORDER BY %s
                LIMIT :size
                OFFSET :offset
                """,TABLE, PageHelper.orderBy(pageable.getSort()));

        var posts = namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);

        return new PageImpl<>(posts, pageable, getCount(memberId));
    }

    private Long getCount(Long memberId){
        var params = new MapSqlParameterSource()
                .addValue("memberId",memberId);
        var sql = String.format("""
                SELECT count(id)
                FROM %s
                WHERE memberId = :memberId""", TABLE);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    public List<Post> findAllByMemberIdAndOrderByIdDesc(Long memberId, int size){
        var params = new MapSqlParameterSource()
                .addValue("memberId",memberId)
                .addValue("size", size);

        var sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId = :memberId
                ORDER BY id DESC
                LIMIT :size
                """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByInMemberIdAndOrderByIdDesc(List<Long> memberIds, int size){
        if (memberIds.isEmpty()){
            return List.of();
        }

        var params = new MapSqlParameterSource()
                .addValue("memberIds",memberIds)
                .addValue("size", size);

        var sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId in (:memberIds)
                ORDER BY id DESC
                LIMIT :size
                """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByLessThanIdMemberIdAndOrderByIdDesc(Long id, Long memberId, int size){
        var params = new MapSqlParameterSource()
                .addValue("memberId",memberId)
                .addValue("id", id)
                .addValue("size", size);

        var sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId = :memberId and id < :id
                ORDER BY id DESC
                LIMIT :size
                """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByLessThanIdAndInMemberIdAndOrderByIdDesc(Long id, List<Long> memberIds, int size){
        if (memberIds.isEmpty()){
            return List.of();
        }

        var params = new MapSqlParameterSource()
                .addValue("memberIds",memberIds)
                .addValue("id", id)
                .addValue("size", size);

        var sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId in (:memberIds) and id < :id
                ORDER BY id DESC
                LIMIT :size
                """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByInId(List<Long> ids){
        if(ids.isEmpty())
            return List.of();

        var params = new MapSqlParameterSource()
                .addValue("ids", ids);

        var sql = String.format("""
                SELECT *
                FROM %s
                WHERE id in (:ids)
                """, TABLE);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }


    public void bulkInsert(List<Post> posts) {
        var sql = String.format("""
                INSERT INTO `%s` (memberId, contents, createdDate, createdAt)
                VALUES (:memberId, :contents, :createdDate, :createdAt)
                """, TABLE);

        SqlParameterSource[] params = posts.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sql, params);
    }
}