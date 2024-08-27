package com.example.bulletinkotlin.service

import com.example.bulletinkotlin.dto.*
import com.example.bulletinkotlin.entity.Board
import com.example.bulletinkotlin.entity.Comment
import com.example.bulletinkotlin.enum.DeleteStatus
import com.example.bulletinkotlin.enum.WriteStatus
import com.example.bulletinkotlin.repository.BoardRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class BoardService(private val boardRepository: BoardRepository) {
    @Transactional
    fun findBoards() : List<GetBoardResultDto> = boardRepository.findAll().map { convertToResult(it) }

    fun findBoardById(id : Long) : GetBoardResultDto? {
        val board = boardRepository.findById(id).orElse(null)
        return board?.let { convertToResult(it) }
    }

    // write
    fun write(boardInfo: WriteBoardRequestDto): WriteBoardResultDto {
        return try {
            val board: Board = boardRepository.save(makeBoard(boardInfo))
            WriteBoardResultDto(
                id = board.id!!,
                status = WriteStatus.SUCCESS
            )
        } catch (e: RuntimeException) {
            WriteBoardResultDto(
                id = -1,
                status = WriteStatus.FAILED
            )
        }
    }

    fun deleteBoardById(id: Long) : DeleteResultDto {
        return try {
            if (boardRepository.existsById(id)) {
                boardRepository.deleteById(id)
                DeleteResultDto(
                    id = id,
                    status = DeleteStatus.SUCCESS
                )
            }
            DeleteResultDto(
                id = id,
                status = DeleteStatus.FAILED
            )
        } catch (e: RuntimeException) {
            throw e
        }
    }

//    fun writeBoard(board: Board) : String = boardRepository.save(board)
    fun convertCommentToResult(comment: Comment): GetCommentResultDto {
        return GetCommentResultDto(
            id = comment.id!!,
            boardId = comment.board.id!!,
            writer = comment.writer,
            textContent = comment.textContent,
            firstWritingTime = comment.firstWritingTime,
            lastModifiedTime = comment.lastModifiedTime)
    }

    fun convertToResult(board: Board): GetBoardResultDto{
        val commentResults: List<GetCommentResultDto> = board.comments.map { convertCommentToResult(it) }
        return GetBoardResultDto(
            id = board.id!!,
            title = board.title,
            writer =  board.writer,
            textContent = board.textContent,
            firstWritingTime = board.firstWritingTime!!,
            lastModifiedTime = board.lastModifiedTime!!,
            readingCount = board.readingCount,
            comments = commentResults)
    }

    fun makeBoard(boardInfo: WriteBoardRequestDto) : Board {
        return Board(
            writer = boardInfo.writer,
            title = boardInfo.title,
            textContent = boardInfo.textContent,
        )
    }
    }