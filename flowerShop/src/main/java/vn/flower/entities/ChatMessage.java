package vn.flower.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ChatMessages")
public class ChatMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "SenderId", nullable = false)
  private Account sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ReceiverId", nullable = false)
  private Account receiver;

  @Column(name = "MessageContent", columnDefinition = "NVARCHAR(MAX)", nullable = false)
  private String messageContent;

  @Column(name = "CreatedAt")
  private LocalDateTime createdAt;

  @Column(name = "IsRead")
  private Boolean isRead;

  // getters/setters
  public Long getId(){ return id; }
  public void setId(Long id){ this.id = id; }
  public Account getSender(){ return sender; }
  public void setSender(Account sender){ this.sender = sender; }
  public Account getReceiver(){ return receiver; }
  public void setReceiver(Account receiver){ this.receiver = receiver; }
  public String getMessageContent(){ return messageContent; }
  public void setMessageContent(String messageContent){ this.messageContent = messageContent; }
  public LocalDateTime getCreatedAt(){ return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
  public Boolean getIsRead(){ return isRead; }
  public void setIsRead(Boolean isRead){ this.isRead = isRead; }
}
